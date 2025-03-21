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

    public IslandConfigManager(CommentedFileConfig config) {
        this.config = config;
        loadConfig();
    }

    @Override
    public void loadConfig() {
        this.configVersion = config.getOrElse("config-version", 1);

        this.defaultIslandKey = config.getOrElse("default-island.default-schem-key", "default");

        islandSettingsMap.clear();

        Map<String, Object> islandSection = config.getOrElse("island", new HashMap<>());

        for (String islandType : islandSection.keySet()) {
            CommentedConfig node = config.get("island." + islandType);
            if (node == null) continue;
            String id = node.getOrElse("id", islandType);
            double size = node.getOrElse("size", 50.0);
            int maxMembers = node.getOrElse("max-members", 6);

            IslandSettings islandSettings = new IslandSettings(id, maxMembers, size);
            islandSettingsMap.put(islandType, islandSettings);
        }
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