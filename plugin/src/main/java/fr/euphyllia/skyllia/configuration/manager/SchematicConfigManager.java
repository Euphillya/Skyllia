package fr.euphyllia.skyllia.configuration.manager;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.IndentStyle;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlWriter;
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
    private boolean changed = false;

    public SchematicConfigManager(CommentedFileConfig config) {
        this.config = config;
        loadConfig();
    }

    @Override
    public void loadConfig() {
        changed = false;
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

            String schematicFile = getOrSetDefault(node, "schematic", "default.schem", String.class);
            double height = getOrSetDefault(node, "height", 64.0, Double.class);
            boolean ignoreAirBlocks = getOrSetDefault(node, "ignore-air-blocks", true, Boolean.class);
            boolean copyEntities = getOrSetDefault(node, "copy-entities", true, Boolean.class);

            schematicMap
                    .computeIfAbsent(islandType, k -> new HashMap<>())
                    .put(worldName, new SchematicSetting(height, schematicFile, ignoreAirBlocks, copyEntities));
        }

        if (schematicMap.isEmpty()) {
            log.warn("[Skyllia] No schematics loaded from schematics.toml!");
        }

        if (changed) {
            TomlWriter tomlWriter = new TomlWriter();
            tomlWriter.setIndent(IndentStyle.NONE);
            tomlWriter.write(config, config.getFile(), WritingMode.REPLACE);
        }
    }

    @Override
    public void reloadFromDisk() {
        config.load();
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrSetDefault(CommentedConfig node, String path, T defaultValue, Class<T> expectedClass) {
        Object value = node.get(path);
        if (value == null) {
            node.set(path, defaultValue);
            changed = true;
            return defaultValue;
        }

        if (expectedClass.isInstance(value)) {
            return (T) value;
        }

        if (expectedClass == Long.class && value instanceof Integer) {
            return (T) Long.valueOf((Integer) value);
        }

        if (expectedClass == Float.class && value instanceof Double) {
            return (T) Float.valueOf(((Double) value).floatValue());
        }

        throw new IllegalStateException("Cannot convert path '" + path + "': found " + value.getClass().getSimpleName() + ", expected " + expectedClass.getSimpleName());
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOrSetDefault(String path, T defaultValue, Class<T> expectedClass) {
        Object value = config.get(path);
        if (value == null) {
            config.set(path, defaultValue);
            changed = true;
            return defaultValue;
        }

        if (expectedClass.isInstance(value)) {
            return (T) value;
        }

        // Cas spécial : Integer → Long
        if (expectedClass == Long.class && value instanceof Integer) {
            return (T) Long.valueOf((Integer) value);
        }

        // Cas spécial : Double → Float
        if (expectedClass == Float.class && value instanceof Double) {
            return (T) Float.valueOf(((Double) value).floatValue());
        }

        throw new IllegalStateException("Cannot convert value at path '" + path + "' from " + value.getClass().getSimpleName() + " to " + expectedClass.getSimpleName());
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