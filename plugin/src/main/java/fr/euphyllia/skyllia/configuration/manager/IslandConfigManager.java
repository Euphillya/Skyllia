package fr.euphyllia.skyllia.configuration.manager;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fr.euphyllia.skyllia.api.skyblock.model.IslandSettings;
import fr.euphyllia.skyllia.managers.ConfigManager;

import java.util.HashMap;
import java.util.Map;

public class IslandConfigManager implements ConfigManager {

    private final Map<String, IslandSettings> islandSettingsMap = new HashMap<>();
    private final CommentedFileConfig config;
    private int configVersion;
    private String defaultIslandKey;
    private boolean changed = false;

    public IslandConfigManager(CommentedFileConfig config) {
        this.config = config;
        loadConfig();
    }

    @Override
    public void loadConfig() {
        changed = false;

        this.configVersion = getOrSetDefault("config-version", 1, Integer.class);
        this.defaultIslandKey = getOrSetDefault("default-island.default-schem-key", "default", String.class);

        islandSettingsMap.clear();

        CommentedConfig islandSection = config.get("island");
        if (islandSection != null) {
            for (String islandType : islandSection.valueMap().keySet()) {
                CommentedConfig node = islandSection.get(islandType);
                if (node == null) continue;

                String id = getOrSetDefault("island." + islandType + ".id", islandType, String.class);
                double size = getOrSetDefault("island." + islandType + ".size", 50.0, Double.class);
                int maxMembers = getOrSetDefault("island." + islandType + ".max-members", 6, Integer.class);

                IslandSettings islandSettings = new IslandSettings(id, maxMembers, size);
                islandSettingsMap.put(islandType, islandSettings);
            }
        }
        if (changed) config.save();
    }

    @Override
    public void reloadFromDisk() {
        config.load();
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
            return (T) value; // Bonne instance directement
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


    public int getConfigVersion() {
        return configVersion;
    }

    public String getDefaultIslandKey() {
        return defaultIslandKey;
    }

    public IslandSettings getIslandSettings(String islandType) {
        return islandSettingsMap.get(islandType);
    }

    public Map<String, IslandSettings> getIslandSettingsMap() {
        return islandSettingsMap;
    }
}