package fr.euphyllia.skyfolia.utils;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.model.IslandType;
import fr.euphyllia.skyfolia.api.skyblock.model.SchematicWorld;
import fr.euphyllia.skyfolia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import fr.euphyllia.skyfolia.configuration.LanguageToml;
import fr.euphyllia.skyfolia.configuration.section.WorldConfig;
import fr.euphyllia.skyfolia.utils.nms.v1_20_R2.PlayerNMS;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

    public static boolean isWorldIsland(String worldName) {
        return getWorldConfigs().stream().anyMatch(wc -> wc.name().equalsIgnoreCase(worldName));
    }

    public static List<WorldConfig> getWorldConfigs() {
        return ConfigToml.worldConfigs;
    }
}
