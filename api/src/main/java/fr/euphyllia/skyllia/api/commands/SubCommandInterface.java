package fr.euphyllia.skyllia.api.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface for defining sub-commands in a command handling system.
 * <p>
 * Implementations of this interface are expected to handle the execution
 * of sub-commands and provide tab completion suggestions.
 * </p>
 */
public interface SubCommandInterface {

    /**
     * Handles the execution of a sub-command.
     *
     * @param plugin the {@link Plugin} instance that is executing the command
     * @param sender the {@link CommandSender} who issued the command
     * @param args   the arguments provided with the command
     * @return {@code true} if the command was successfully handled, {@code false} otherwise
     */
    boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args);

    /**
     * Provides tab completion suggestions for the sub-command.
     *
     * @param plugin the {@link Plugin} instance that is executing the command
     * @param sender the {@link CommandSender} who is tab completing
     * @param args   the arguments provided with the command so far
     * @return a {@link List} of suggestions for tab completion, or {@code null} if no suggestions are available
     */
    @NotNull
    List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args);
}
