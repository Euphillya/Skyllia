package fr.euphyllia.skyllia.utils;

import fr.euphyllia.skyllia.api.skyblock.model.IslandSettings;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicSetting;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IslandUtils {

    public static @Nullable IslandSettings getIslandSettings(String name) {
        try {
            if (name == null) {
                return ConfigToml.islandSettingsMap.getOrDefault(ConfigToml.defaultSchematicKey,
                        ConfigToml.islandSettingsMap.values().stream().toList().getFirst());
            } else {
                return ConfigToml.islandSettingsMap.getOrDefault(name, getIslandSettings(null));
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static Map<String, SchematicSetting> getSchematic(@NotNull String name) {
        try {
            return ConfigToml.schematicWorldMap.getOrDefault(name.toLowerCase(), new ConcurrentHashMap<>());
        } catch (Exception e) {
            return null;
        }
    }
}
