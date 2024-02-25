package fr.euphyllia.skyllia.commands.common;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.utils.scheduler.SchedulerTask;
import fr.euphyllia.skyllia.api.utils.scheduler.model.SchedulerType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkylliaCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public SkylliaCommand(Main main) {
        this.plugin = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 0) {
            String subCommand = args[0].trim().toLowerCase();
            String[] listArgs = Arrays.copyOfRange(args, 1, args.length);
            SubCommands subCommands = SubCommands.subCommandByName(subCommand);
            if (subCommands == null) {
                return false;
            }
            SkylliaAPI.getSchedulerTask()
                    .getScheduler(SchedulerTask.SchedulerSoft.NATIVE)
                    .execute(SchedulerType.ASYNC, schedulerTask -> subCommands.getSubCommandInterface().onCommand(this.plugin, sender, command, label, listArgs));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 1) {
            SubCommands[] subCommand = SubCommands.values();
            for (SubCommands sub : subCommand) {
                tab.add(sub.name().toLowerCase());
            }
        } else {
            String subCommand = args[0].trim().toLowerCase();
            String[] listArgs = Arrays.copyOfRange(args, 1, args.length);
            SubCommands subCommands = SubCommands.subCommandByName(subCommand);
            if (subCommands != null) {
                return subCommands.getSubCommandInterface().onTabComplete(this.plugin, sender, command, label, listArgs);
            }
        }
        return tab;
    }

}