package fr.euphyllia.skyllia.configuration.manager;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicSetting;
import fr.euphyllia.skyllia.managers.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class SchematicConfigManager implements ConfigManager {

    private static final Logger log = LogManager.getLogger(SchematicConfigManager.class);
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
        schematicMap.clear();
        for (String key : config.valueMap().keySet()) {
            Object value = config.valueMap().get(key);
            if (!(value instanceof CommentedConfig node)) {
                log.warn("[Skyllia] Key '{}' is not a CommentedConfig (type: {})", key, value == null ? "null" : value.getClass().getName());
                continue;
            }

            int index = key.lastIndexOf('.');
            if (index <= 0) continue;

            String islandType = key.substring(0, index);
            String worldName = key.substring(index + 1);

            String schematicFile = node.getOrElse("schematic", "default.schem");
            double height = node.getOrElse("height", 64.0);
            boolean ignoreAirBlocks = node.getOrElse("ignore-air-blocks", true);
            boolean copyEntities = node.getOrElse("copy-entities", true);

            schematicMap
                    .computeIfAbsent(islandType, k -> new HashMap<>())
                    .put(worldName, new SchematicSetting(height, schematicFile, ignoreAirBlocks, copyEntities));
        }
        if (schematicMap.isEmpty()) {
            log.warn("[Skyllia] No schematics loaded from schematics.toml!");
        }
    }

    public SchematicSetting getSchematicSetting(String islandType, String worldName) {
        return schematicMap
                .getOrDefault(islandType, new HashMap<>())
                .getOrDefault(worldName, new SchematicSetting(64.0, "./schematics/default.schem", true, true));
    }

    public Map<String, Map<String, SchematicSetting>> getSchematics() {
        return schematicMap;
    }
}