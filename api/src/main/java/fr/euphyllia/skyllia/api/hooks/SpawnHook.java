package fr.euphyllia.skyllia.api.hooks;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface SpawnHook {

    boolean isAvailable();

    @Nullable Location getSpawnLocation(Player player);

    default boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
