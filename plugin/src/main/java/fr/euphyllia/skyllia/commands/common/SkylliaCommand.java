package fr.euphyllia.skyllia.commands.common;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.commands.SkylliaCommandInterface;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.commands.SubCommandRegistry;
import fr.euphyllia.skyllia.commands.common.subcommands.*;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class SkylliaCommand implements SkylliaCommandInterface {

    private final Skyllia plugin;
    private final SubCommandRegistry registry;

    public SkylliaCommand(Skyllia Skyllia) {
        this.plugin = Skyllia;
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
        registry.registerSubCommand(new DebugSubCommand(), "debug");
    }

    @Override
    public void execute(CommandSourceStack sender, String[] args) {
        if (args.length != 0) {
            String subCommand = args[0].trim().toLowerCase();
            String[] listArgs = Arrays.copyOfRange(args, 1, args.length);
            SubCommandInterface subCommandInterface = registry.getSubCommandByName(subCommand);
            if (subCommandInterface == null) {
                ConfigLoader.language.sendMessage(sender.getSender(), "misc.unknown-command");
                return;
            }
            Bukkit.getAsyncScheduler().runNow(this.plugin, task ->
                    subCommandInterface.onCommand(this.plugin, sender.getSender(), listArgs));
        }
    }

    @Override
    public Collection<String> suggest(CommandSourceStack sender, String[] args) {
        Set<String> commands = registry.getCommandMap().keySet();
        if (args.length == 0) {
            return commands;
        } else if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();
            return commands.stream().filter(command -> command.toLowerCase().startsWith(partial)).toList();
        } else {
            String subCommand = args[0].trim().toLowerCase();
            String[] listArgs = Arrays.copyOfRange(args, 1, args.length);
            SubCommandInterface subCommandInterface = registry.getSubCommandByName(subCommand);
            if (subCommandInterface != null) {
                return subCommandInterface.onTabComplete(this.plugin, sender.getSender(), listArgs);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public @Nullable String permission() {
        return null;
    }
}
