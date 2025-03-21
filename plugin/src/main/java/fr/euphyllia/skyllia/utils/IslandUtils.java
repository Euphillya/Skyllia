package fr.euphyllia.skyllia.utils;

import fr.euphyllia.skyllia.api.skyblock.model.IslandSettings;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicSetting;

import fr.euphyllia.skyllia.configuration.ConfigLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IslandUtils {

    public static @Nullable IslandSettings getIslandSettings(String name) {
        try {
            if (name == null || name.isEmpty()) {
                return ConfigLoader.islandManager.getIslandSettings(ConfigLoader.islandManager.getDefaultIslandKey());
            } else {
                return ConfigLoader.islandManager.getIslandSettings(name);
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static Map<String, SchematicSetting> getSchematic(@NotNull String name) {
        try {
            return ConfigLoader.schematicManager.getSchematics().get(name);
        } catch (Exception e) {
            return null;
        }
    }
}
