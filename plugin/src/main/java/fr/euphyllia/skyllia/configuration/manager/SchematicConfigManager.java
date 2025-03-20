package fr.euphyllia.skyllia.configuration.manager;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fr.euphyllia.skyllia.managers.ConfigManager;

import java.util.HashMap;
import java.util.Map;

public class SchematicConfigManager implements ConfigManager {
    private final Map<String, Map<String, String>> schematics = new HashMap<>();

    private final CommentedFileConfig config;
    public SchematicConfigManager(CommentedFileConfig config) {
        this.config = config;
        loadConfig();
    }

    @Override
    public void loadConfig() {
        for (Object obj : config.getOrElse("", new HashMap<>()).keySet()) {
            String islandType = (String) obj;
            schematics.put(islandType, config.getOrElse(islandType, new HashMap<>()));
        }
    }

    public String getSchematic(String islandType, String world) {
        return schematics.getOrDefault(islandType, new HashMap<>()).getOrDefault(world, "default.schem");
    }

    public Map<String, Map<String, String>> getSchematics() {
        return schematics;
    }
}