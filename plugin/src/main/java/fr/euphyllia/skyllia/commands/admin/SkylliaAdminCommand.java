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

import java.util.*;
import java.util.stream.Collectors;

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
            Bukkit.getAsyncScheduler().runNow(this.plugin, task ->
                    subCommandInterface.onCommand(this.plugin, sender.getSender(), listArgs));
        }
        return;
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

    private void registerDefaultCommands() {
        registry.registerSubCommand(new ForceDeleteSubCommands(), "force_delete", "forcedelete");
        registry.registerSubCommand(new SetMaxMembersSubCommands(), "set_max_member", "setmaxmembers");
        registry.registerSubCommand(new SetSizeSubCommands(), "set_size", "setsize");
        registry.registerSubCommand(new ForceTransferSubCommands(), "force_transfer", "forcetransfer");
    }
}