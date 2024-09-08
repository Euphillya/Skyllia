package fr.euphyllia.skyllia.api.commands;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Abstract class for managing the registration and retrieval of sub-commands.
 * <p>
 * This class provides an abstract mechanism for registering sub-commands with their aliases,
 * retrieving sub-commands by their names, and accessing a map of all registered sub-commands.
 * Implementations of this class should provide concrete behavior for these methods.
 * </p>
 */
public abstract class SubCommandRegistry {

    /**
     * Registers a sub-command with its associated aliases.
     *
     * @param subCommand the {@link SubCommandInterface} implementation to register
     * @param aliases    one or more aliases for the sub-command
     */
    public abstract void registerSubCommand(@NotNull SubCommandInterface subCommand, @NotNull String... aliases);

    /**
     * Retrieves a sub-command by its name or alias.
     *
     * @param name the name or alias of the sub-command
     * @return the {@link SubCommandInterface} implementation associated with the given name, or {@code null} if not found
     */
    public abstract SubCommandInterface getSubCommandByName(@NotNull String name);

    /**
     * Retrieves a map of all registered sub-commands and their aliases.
     *
     * @return a {@link Map} where the key is the alias of the sub-command (in lower case) and the value is the {@link SubCommandInterface} implementation
     */
    public abstract Map<String, SubCommandInterface> getCommandMap();
}
