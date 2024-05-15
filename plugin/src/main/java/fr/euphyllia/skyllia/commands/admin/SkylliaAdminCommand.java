package fr.euphyllia.skyllia.commands.admin;

import fr.euphyllia.energie.model.SchedulerType;
import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.commands.SkylliaCommandInterface;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkylliaAdminCommand implements SkylliaCommandInterface {

    private final Main plugin;

    public SkylliaAdminCommand(Main main) {
        this.plugin = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 0) {
            String subCommand = args[0].trim().toLowerCase();
            String[] listArgs = Arrays.copyOfRange(args, 1, args.length);
            SubAdminCommands subCommands = SubAdminCommands.subCommandByName(subCommand);
            if (subCommands == null) {
                return false;
            }
            SkylliaAPI.getNativeScheduler()
                    .runTask(SchedulerType.ASYNC, schedulerTask -> {
                        subCommands.getSubCommandInterface().onCommand(this.plugin, sender, command, label, listArgs);
                    });
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 1) {
            SubAdminCommands[] subCommand = SubAdminCommands.values();
            for (SubAdminCommands sub : subCommand) {
                tab.add(sub.name().toLowerCase());
            }
        } else {
            String subCommand = args[0].trim().toLowerCase();
            String[] listArgs = Arrays.copyOfRange(args, 1, args.length);
            SubAdminCommands subCommands = SubAdminCommands.subCommandByName(subCommand);
            if (subCommands != null) {
                return subCommands.getSubCommandInterface().onTabComplete(this.plugin, sender, command, label, listArgs);
            }
        }
        return tab;
    }

}