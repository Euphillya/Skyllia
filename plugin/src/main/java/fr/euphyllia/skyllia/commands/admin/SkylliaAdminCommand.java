package fr.euphyllia.skyllia.commands.admin;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.commands.SkylliaCommandInterface;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.commands.admin.subcommands.ForceDeleteSubCommands;
import fr.euphyllia.skyllia.commands.admin.subcommands.SetMaxMembersSubCommands;
import fr.euphyllia.skyllia.commands.admin.subcommands.SetSizeSubCommands;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkylliaAdminCommand implements SkylliaCommandInterface {

    private final Main plugin;

    public SkylliaAdminCommand(Main main) {
        this.plugin = main;
        registerDefaultCommands();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 0) {
            String subCommand = args[0].trim().toLowerCase();
            String[] listArgs = Arrays.copyOfRange(args, 1, args.length);
            SubCommandInterface subCommandInterface = this.plugin.getAdminCommandRegistry().getSubCommandByName(subCommand);
            if (subCommandInterface == null) {
                sender.sendMessage("Cette sous-commande n'existe pas.");
                return false;
            }
            if (ConfigToml.useVirtualThread) {
                Thread.startVirtualThread(() -> {
                    subCommandInterface.onCommand(this.plugin, sender, command, label, listArgs);
                });
            } else {
                Bukkit.getAsyncScheduler().runNow(this.plugin, task ->
                        subCommandInterface.onCommand(this.plugin, sender, command, label, listArgs));
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 1) {
            tab.addAll(this.plugin.getAdminCommandRegistry().getCommandMap().keySet());
        } else {
            String subCommand = args[0].trim().toLowerCase();
            String[] listArgs = Arrays.copyOfRange(args, 1, args.length);
            SubCommandInterface subCommandInterface = this.plugin.getAdminCommandRegistry().getSubCommandByName(subCommand);
            if (subCommandInterface != null) {
                return subCommandInterface.onTabComplete(this.plugin, sender, command, label, listArgs);
            }
        }
        return tab;
    }

    private void registerDefaultCommands() {
        this.plugin.getAdminCommandRegistry().registerSubCommand(new ForceDeleteSubCommands(), "force_delete", "forcedelete");
        this.plugin.getAdminCommandRegistry().registerSubCommand(new SetMaxMembersSubCommands(), "set_max_member", "setmaxmembers");
        this.plugin.getAdminCommandRegistry().registerSubCommand(new SetSizeSubCommands(), "set_size", "setsize");
    }
}