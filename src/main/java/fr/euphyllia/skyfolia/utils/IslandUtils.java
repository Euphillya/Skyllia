package fr.euphyllia.skyfolia.utils;

import fr.euphyllia.skyfolia.api.skyblock.model.IslandType;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import org.jetbrains.annotations.Nullable;

public class IslandUtils {

    public static @Nullable IslandType getIslandType(String name) {
        try {
            if (name == null) {
                return ConfigToml.islandTypes.values().stream().toList().get(0);
            } else {
                return ConfigToml.islandTypes.getOrDefault(name, null);
            }
        } catch (Exception e) {
            return null;
        }
    }
}
