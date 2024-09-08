package fr.euphyllia.skyllia.commands.common;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.commands.SkylliaCommandInterface;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.commands.common.subcommands.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkylliaCommand implements SkylliaCommandInterface {

    private final Main plugin;

    public SkylliaCommand(Main main) {
        this.plugin = main;
        registerDefaultCommands();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 0) {
            String subCommand = args[0].trim().toLowerCase();
            String[] listArgs = Arrays.copyOfRange(args, 1, args.length);
            SubCommandInterface subCommandInterface = this.plugin.getCommandRegistry().getSubCommandByName(subCommand);
            if (subCommandInterface == null) {
                sender.sendMessage("Cette sous-commande n'existe pas.");
                return false;
            }
            Bukkit.getAsyncScheduler().runNow(this.plugin, task ->
                    subCommandInterface.onCommand(this.plugin, sender, command, label, listArgs));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 1) {
            tab.addAll(this.plugin.getCommandRegistry().getCommandMap().keySet());
        } else {
            String subCommand = args[0].trim().toLowerCase();
            String[] listArgs = Arrays.copyOfRange(args, 1, args.length);
            SubCommandInterface subCommandInterface = this.plugin.getCommandRegistry().getSubCommandByName(subCommand);
            if (subCommandInterface != null) {
                return subCommandInterface.onTabComplete(this.plugin, sender, command, label, listArgs);
            }
        }
        return tab;
    }

    private void registerDefaultCommands() {
        this.plugin.getCommandRegistry().registerSubCommand(new AccessSubCommand(), "access");
        this.plugin.getCommandRegistry().registerSubCommand(new BanSubCommand(), "ban");
        this.plugin.getCommandRegistry().registerSubCommand(new SetBiomeSubCommand(), "biome");
        this.plugin.getCommandRegistry().registerSubCommand(new CreateSubCommand(), "create");
        this.plugin.getCommandRegistry().registerSubCommand(new DeleteSubCommand(), "delete");
        this.plugin.getCommandRegistry().registerSubCommand(new DelWarpSubCommand(), "delwarp");
        this.plugin.getCommandRegistry().registerSubCommand(new DemoteSubCommand(), "demote");
        this.plugin.getCommandRegistry().registerSubCommand(new ExpelSubCommand(), "expel");
        this.plugin.getCommandRegistry().registerSubCommand(new GameRuleSubCommand(), "gamerule");
        this.plugin.getCommandRegistry().registerSubCommand(new HomeSubCommand(), "home", "go", "tp");
        this.plugin.getCommandRegistry().registerSubCommand(new InviteSubCommand(), "invite", "add");
        this.plugin.getCommandRegistry().registerSubCommand(new KickSubCommand(), "kick");
        this.plugin.getCommandRegistry().registerSubCommand(new LeaveSubCommand(), "leave");
        this.plugin.getCommandRegistry().registerSubCommand(new PermissionSubCommand(), "permission");
        this.plugin.getCommandRegistry().registerSubCommand(new PromoteSubCommand(), "promote");
        this.plugin.getCommandRegistry().registerSubCommand(new TPSSubCommand(), "tps", "lag", "mspt");
        this.plugin.getCommandRegistry().registerSubCommand(new TransferSubCommand(), "transfer");
        this.plugin.getCommandRegistry().registerSubCommand(new TrustSubCommand(), "trust");
        this.plugin.getCommandRegistry().registerSubCommand(new SetHomeSubCommand(), "sethome");
        this.plugin.getCommandRegistry().registerSubCommand(new SetWarpSubCommand(), "setwarp");
        this.plugin.getCommandRegistry().registerSubCommand(new UnbanSubCommand(), "unban");
        this.plugin.getCommandRegistry().registerSubCommand(new UntrustSubCommand(), "untrust");
        this.plugin.getCommandRegistry().registerSubCommand(new VisitSubCommand(), "visit");
        this.plugin.getCommandRegistry().registerSubCommand(new WarpSubCommand(), "warp");
    }
}
