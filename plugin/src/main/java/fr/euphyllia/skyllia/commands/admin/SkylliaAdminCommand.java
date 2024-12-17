package fr.euphyllia.skyllia.commands.admin;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.commands.SkylliaCommandInterface;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.commands.SubCommandRegistry;
import fr.euphyllia.skyllia.commands.admin.subcommands.ForceDeleteSubCommands;
import fr.euphyllia.skyllia.commands.admin.subcommands.ForceTransferSubCommands;
import fr.euphyllia.skyllia.commands.admin.subcommands.SetMaxMembersSubCommands;
import fr.euphyllia.skyllia.commands.admin.subcommands.SetSizeSubCommands;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import io.papermc.paper.command.brigadier.CommandSourceStack;
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
    private final SubCommandRegistry registry;

    public SkylliaAdminCommand(Main main) {
        this.plugin = main;
        this.registry = this.plugin.getAdminCommandRegistry();
        registerDefaultCommands();
    }

    @Override
    public void execute(CommandSourceStack sender, String @NotNull [] args) {
        if (!sender.getSender().hasPermission("skyllia.admins.commands")) {
            LanguageToml.sendMessage(sender.getSender(), LanguageToml.messagePlayerPermissionDenied);
            return;
        }
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
        return;
    }

    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 1) {
            tab.addAll(registry.getCommandMap().keySet());
        } else {
            String subCommand = args[0].trim().toLowerCase();
            String[] listArgs = Arrays.copyOfRange(args, 1, args.length);
            SubCommandInterface subCommandInterface = registry.getSubCommandByName(subCommand);
            if (subCommandInterface != null) {
                return subCommandInterface.onTabComplete(this.plugin, sender, listArgs);
            }
        }
        return tab;
    }

    private void registerDefaultCommands() {
        registry.registerSubCommand(new ForceDeleteSubCommands(), "force_delete", "forcedelete");
        registry.registerSubCommand(new SetMaxMembersSubCommands(), "set_max_member", "setmaxmembers");
        registry.registerSubCommand(new SetSizeSubCommands(), "set_size", "setsize");
        registry.registerSubCommand(new ForceTransferSubCommands(), "force_transfer", "forcetransfer");
    }
}