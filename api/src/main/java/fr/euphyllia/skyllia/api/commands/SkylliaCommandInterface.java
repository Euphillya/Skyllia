package fr.euphyllia.skyllia.api.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

/**
 * Interface for defining a command that implements both command execution and tab completion.
 * <p>
 * This interface combines the functionality of {@link CommandExecutor} and {@link TabCompleter},
 * allowing the implementing class to handle command execution and provide tab completion suggestions.
 * </p>
 */
public interface SkylliaCommandInterface extends CommandExecutor, TabCompleter {
}
