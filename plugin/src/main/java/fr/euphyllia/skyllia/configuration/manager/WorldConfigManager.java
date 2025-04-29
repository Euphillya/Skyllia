package fr.euphyllia.skyllia.configuration.manager;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.managers.ConfigManager;

import java.util.HashMap;
import java.util.Map;

public class WorldConfigManager implements ConfigManager {

    /**
     * Map<nomDuMonde, WorldConfig>
     * Exemple :
     * "sky-overworld" -> (Environnements.NORMAL, "sky-nether", "sky-end")
     */
    private final Map<String, WorldConfig> worldConfigs = new HashMap<>();
    private final CommentedFileConfig config;
    private boolean suppressWarnNetherEndWorld = false;
    private boolean changed = false;

    public WorldConfigManager(CommentedFileConfig config) {
        this.config = config;
        loadConfig();
    }

    @Override
    public void loadConfig() {
        changed = false;
        this.suppressWarnNetherEndWorld = getOrSetDefault("suppress-warning-nether-end", false, Boolean.class);

        worldConfigs.clear();

        CommentedConfig worlds = config.get("worlds");
        if (worlds != null) {
            for (String worldName : worlds.valueMap().keySet()) {
                CommentedConfig node = worlds.get(worldName);
                if (node == null) continue;

                String basePath = "worlds." + worldName + ".";

                String envString = getOrSetDefault(basePath + "environment", "NORMAL", String.class);
                String portalNether = getOrSetDefault(basePath + "portal-nether", "sky-nether", String.class);
                String portalEnd = getOrSetDefault(basePath + "portal-end", "sky-end", String.class);
                String generator = getOrSetDefault(basePath + "generator", "default", String.class);

                WorldConfig wc = new WorldConfig(worldName, envString, portalNether, portalEnd, generator);
                worldConfigs.put(worldName, wc);
            }
        }
        if (changed) {
            config.save();
        }
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

    public WorldConfig getWorldConfig(String worldName) {
        return worldConfigs.get(worldName);
    }

    public Map<String, WorldConfig> getWorldConfigs() {
        return worldConfigs;
    }

    public boolean isSuppressWarnNetherEndWorld() {
        return suppressWarnNetherEndWorld;
    }
}