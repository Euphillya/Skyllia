package fr.euphyllia.skyfolia.commands;

import fr.euphyllia.skyfolia.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface SubCommandInterface {
    boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args);

    @Nullable List<String> onTabComplete(@NotNull Main plugin,@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args);

}
