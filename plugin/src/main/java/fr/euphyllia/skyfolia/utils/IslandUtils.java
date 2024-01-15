package fr.euphyllia.skyfolia.utils;

import fr.euphyllia.skyfolia.api.skyblock.model.IslandType;
import fr.euphyllia.skyfolia.api.skyblock.model.SchematicWorld;
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

    public static @Nullable SchematicWorld getSchematic(String name) {
        try {
            if (name == null) {
                return ConfigToml.schematicWorldMap.values().stream().toList().get(0);
            } else {
                return ConfigToml.schematicWorldMap.getOrDefault(name, null);
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isWorldIsland(String worldName) {
        return ConfigToml.worldConfigs.stream().anyMatch(wc -> wc.name().equalsIgnoreCase(worldName));
    }
}
