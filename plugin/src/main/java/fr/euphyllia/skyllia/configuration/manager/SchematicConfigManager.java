package fr.euphyllia.skyllia.configuration.manager;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicSetting;
import fr.euphyllia.skyllia.managers.ConfigManager;

import java.util.HashMap;
import java.util.Map;

public class SchematicConfigManager implements ConfigManager {

    /**
     * Map<islandType, Map<worldName, SchematicSetting>>
     * Exemple :
     * "my_first_island" -> ("sky-overworld" -> SchematicSetting(64.0, "./schematics/my_first_island.schem"), ...)
     */
    private final Map<String, Map<String, SchematicSetting>> schematicMap = new HashMap<>();

    private final CommentedFileConfig config;

    public SchematicConfigManager(CommentedFileConfig config) {
        this.config = config;
        loadConfig();
    }

    @Override
    public void loadConfig() {
        for (String key : config.valueMap().keySet()) {
            CommentedConfig node = config.get(key);
            if (node == null) continue;
            int index = key.lastIndexOf('.');
            if (index <= 0) continue;
            String islandType = key.substring(0, index);
            String worldName = key.substring(index + 1);

            String schematicFile = node.getOrElse("schematic", "default.schem");
            double height = node.getOrElse("height", 64.0);

            schematicMap
                    .computeIfAbsent(islandType, k -> new HashMap<>())
                    .put(worldName, new SchematicSetting(height, schematicFile));
        }
    }

    public SchematicSetting getSchematicSetting(String islandType, String worldName) {
        return schematicMap
                .getOrDefault(islandType, new HashMap<>())
                .getOrDefault(worldName, new SchematicSetting(64.0, "default.schem"));
    }

    public Map<String, Map<String, SchematicSetting>> getSchematics() {
        return schematicMap;
    }
}