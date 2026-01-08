package fr.euphyllia.skyllia.api.hooks;

import org.bukkit.plugin.Plugin;

public interface ServerHook  {

    String name();

    boolean isAvailable();

    void register(Plugin skylliaPlugin);

    default boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
