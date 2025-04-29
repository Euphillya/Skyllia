package fr.euphyllia.skyllia.managers;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;

public interface ConfigManager {
    /**
     * Load and validate the configuration from the file.
     * Missing keys are added with default values.
     */

    void loadConfig() throws DatabaseException;

    /**
     * Get the value from the configuration file or set a default value if it doesn't exist.
     *
     * @param path          The path to the value in the configuration file.
     * @param defaultValue  The default value to set if the key doesn't exist.
     * @param expectedClass The expected class of the value.
     * @return The value from the configuration file or the default value.
     */
    <T> T getOrSetDefault(String path, T defaultValue, Class<T> expectedClass);

    default void reloadFromDisk() {
    }
}
