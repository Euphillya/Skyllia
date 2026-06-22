package fr.euphyllia.skyllia.api.hooks;

import org.bukkit.plugin.Plugin;

public interface PluginHook {

    /**
     * Tests whether a class is present on the classpath.
     *
     * @param className fully qualified class name to look up.
     * @return {@code true} if the class can be loaded.
     */
    static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Human-readable name used in log messages.
     */
    String name();

    /**
     * Returns {@code true} when the target plugin is present on the classpath
     * and enabled on the server.
     */
    boolean isAvailable();

    /**
     * Registers any event listeners or service bindings required by this hook.
     *
     * @param skylliaPlugin the Skyllia {@link Plugin} instance to register against.
     */
    void register(Plugin skylliaPlugin);

    /**
     * Called when Skyllia is disabled. Override to clean up resources
     * (e.g. unregister configs, cancel tasks).
     * No-op by default.
     */
    default void unregister() {
    }
}
