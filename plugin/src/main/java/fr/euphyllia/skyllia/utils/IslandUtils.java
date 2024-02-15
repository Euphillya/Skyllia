package fr.euphyllia.skyllia.utils;

import fr.euphyllia.skyllia.api.skyblock.model.IslandType;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicSetting;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class IslandUtils {

    @Deprecated(forRemoval = true) // Todo C'est un vieux truc qu'utiliser la version beta qui sera prochainement enlever et remplacer !
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

    public static Map<String, SchematicSetting> getSchematic(@NotNull String name) {
        try {
            return ConfigToml.schematicWorldMap.getOrDefault(name.toLowerCase(), new HashMap<>());
        } catch (Exception e) {
            return null;
        }
    }
}
