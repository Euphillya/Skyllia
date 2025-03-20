package fr.euphyllia.skyllia.configuration.manager;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fr.euphyllia.skyllia.api.skyblock.model.IslandSettings;
import fr.euphyllia.skyllia.managers.ConfigManager;

import java.util.HashMap;
import java.util.Map;

public class IslandConfigManager implements ConfigManager {
    private final Map<String, IslandSettings> islandSettingsMap = new HashMap<>();
    private String defaultIslandKey;

    private final CommentedFileConfig config;

    public IslandConfigManager(CommentedFileConfig config) {
        this.config = config;
        loadConfig();
    }

    @Override
    public void loadConfig() {
        this.defaultIslandKey = config.getOrElse("default-island.default-schem-key", "default");
        for (Object obj : config.getOrElse("island", new HashMap<>()).keySet()) {
            String islandType = (String) obj;
            double size = config.getOrElse("island." + islandType + ".size", 50.0);
            int maxMembers = config.getOrElse("island." + islandType + ".max-members", 6);
            islandSettingsMap.put(islandType, new IslandSettings(islandType, maxMembers, size));
        }
    }

    public IslandSettings getIslandSettings(String islandType) {
        return islandSettingsMap.get(islandType);
    }

    public String getDefaultIslandKey() {
        return defaultIslandKey;
    }
}