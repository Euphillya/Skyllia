package fr.euphyllia.skyllia.api.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Interface for defining sub-commands in a command handling system.
 * <p>
 * Implementations of this interface are expected to handle the execution
 * of sub-commands and provide tab completion suggestions.
 * </p>
 */
public interface SubCommandInterface {

    Logger log = LoggerFactory.getLogger(SubCommandInterface.class);

    /**
     * Legacy method to handle the execution of a sub-command.
     * <p>
     * Delegates to {@link #onExecute(Plugin, CommandSender, String[])} and always returns {@code true}.
     * This method is kept for backwards compatibility and is marked {@link Deprecated} for future removal.
     *
     * @param plugin the {@link Plugin} instance that is executing the command
     * @param sender the {@link CommandSender} who issued the command
     * @param args   the arguments provided with the command
     * @return {@code true} always
     * @deprecated Use {@link #onExecute(Plugin, CommandSender, String[])} instead.
     */
    @Deprecated(forRemoval = true)
    default boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        onExecute(plugin, sender, args);
        log.warn("The SubCommandInterface#onCommand() method will be removed! Update your plugin : {}", plugin.getName());
        return true;
    }

    /**
     * Preferred method to handle the execution of a sub-command.
     * <p>
     * Default implementation does nothing. Override this method to implement
     * custom command behavior.
     *
     * @param plugin the {@link Plugin} instance that is executing the command
     * @param sender the {@link CommandSender} who issued the command
     * @param args   the arguments provided with the command
     */
    default void onExecute(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
    }

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

    /**
     * Returns the permission required to execute this sub-command.
     * <p>
     * Default implementation returns an empty string (no permission required).
     *
     * @return the permission node as a {@link String}, or empty if none
     */
    default String permission() {
        return "";
    }
}
