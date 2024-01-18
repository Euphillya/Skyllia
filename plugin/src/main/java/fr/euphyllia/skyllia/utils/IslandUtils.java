package fr.euphyllia.skyllia.utils;

import fr.euphyllia.skyllia.api.skyblock.model.IslandType;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicWorld;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import org.jetbrains.annotations.Nullable;

public class IslandUtils {

    public static @Nullable IslandType getIslandType(String name) {
        try {
            if (name == null) {
                return ConfigToml.islandTypes.values().stream().toList().get(0);
            } else {
                return ConfigToml.islandTypes.getOrDefault(name, getIslandType(null));
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static @Nullable SchematicWorld getSchematic(String name) {
        try {
            if (name == null) {
                return ConfigToml.schematicWorldMap.entrySet().stream().toList().get(0).getValue();
            } else {
                return ConfigToml.schematicWorldMap.getOrDefault(name.toLowerCase(), getSchematic(null));
            }
        } catch (Exception e) {
            return null;
        }
    }
}
