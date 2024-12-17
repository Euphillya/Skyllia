package fr.euphyllia.skyllia.commands.common;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.commands.SkylliaCommandInterface;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.commands.SubCommandRegistry;
import fr.euphyllia.skyllia.commands.common.subcommands.*;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SkylliaCommand implements SkylliaCommandInterface {

    private final Main plugin;
    private final SubCommandRegistry registry;

    public SkylliaCommand(Main main) {
        this.plugin = main;
        this.registry = this.plugin.getCommandRegistry();
        registerDefaultCommands();
    }

    private void registerDefaultCommands() {
        registry.registerSubCommand(new AccessSubCommand(), "access");
        registry.registerSubCommand(new BanSubCommand(), "ban");
        registry.registerSubCommand(new SetBiomeSubCommand(), "biome");
        registry.registerSubCommand(new CreateSubCommand(), "create");
        registry.registerSubCommand(new DeleteSubCommand(), "delete");
        registry.registerSubCommand(new DelWarpSubCommand(), "delwarp");
        registry.registerSubCommand(new DemoteSubCommand(), "demote");
        registry.registerSubCommand(new ExpelSubCommand(), "expel");
        registry.registerSubCommand(new GameRuleSubCommand(), "gamerule");
        registry.registerSubCommand(new HomeSubCommand(), "home", "go", "tp");
        registry.registerSubCommand(new InviteSubCommand(), "invite", "add");
        registry.registerSubCommand(new KickSubCommand(), "kick");
        registry.registerSubCommand(new LeaveSubCommand(), "leave");
        registry.registerSubCommand(new PermissionSubCommand(), "permission");
        registry.registerSubCommand(new PromoteSubCommand(), "promote");
        registry.registerSubCommand(new TPSSubCommand(), "tps", "lag", "mspt");
        registry.registerSubCommand(new TransferSubCommand(), "transfer");
        registry.registerSubCommand(new TrustSubCommand(), "trust");
        registry.registerSubCommand(new SetHomeSubCommand(), "sethome");
        registry.registerSubCommand(new SetWarpSubCommand(), "setwarp");
        registry.registerSubCommand(new UnbanSubCommand(), "unban");
        registry.registerSubCommand(new UntrustSubCommand(), "untrust");
        registry.registerSubCommand(new VisitSubCommand(), "visit");
        registry.registerSubCommand(new WarpSubCommand(), "warp");
    }

    @Override
    public void execute(CommandSourceStack sender, String[] args) {
        if (args.length != 0) {
            String subCommand = args[0].trim().toLowerCase();
            String[] listArgs = Arrays.copyOfRange(args, 1, args.length);
            SubCommandInterface subCommandInterface = registry.getSubCommandByName(subCommand);
            if (subCommandInterface == null) {
                LanguageToml.sendMessage(sender.getSender(), LanguageToml.messageSubCommandsNotExists);
                return;
            }
            if (ConfigToml.useVirtualThread) {
                Thread.startVirtualThread(() -> {
                    subCommandInterface.onCommand(this.plugin, sender.getSender(), listArgs);
                });
            } else {
                Bukkit.getAsyncScheduler().runNow(this.plugin, task ->
                        subCommandInterface.onCommand(this.plugin, sender.getSender(), listArgs));
            }
        }
    }

    @Override
    public Collection<String> suggest(CommandSourceStack sender, String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 0) {
            tab.addAll(registry.getCommandMap().keySet());
        } else {
            System.out.println(Arrays.toString(args));
            String subCommand = args[0].trim().toLowerCase();
            String[] listArgs = Arrays.copyOfRange(args, 1, args.length);
            SubCommandInterface subCommandInterface = registry.getSubCommandByName(subCommand);
            if (subCommandInterface != null) {
                return subCommandInterface.onTabComplete(this.plugin, sender.getSender(), listArgs);
            }
        }
        return tab;
    }
}
