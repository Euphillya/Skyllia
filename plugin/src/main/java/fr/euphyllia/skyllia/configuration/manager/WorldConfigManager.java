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

    public WorldConfigManager(CommentedFileConfig config) {
        this.config = config;
        loadConfig();
    }

    @Override
    public void loadConfig() {
        this.suppressWarnNetherEndWorld = config.getOrElse("suppress-warning-nether-end", false);

        worldConfigs.clear();

        CommentedConfig worlds = config.get("worlds");
        if (worlds != null) {
            for (String worldName : worlds.valueMap().keySet()) {
                CommentedConfig node = worlds.get(worldName);
                if (node == null) continue;

                String envString = node.getOrElse("environment", "NORMAL");
                String portalNether = node.getOrElse("portal-nether", "sky-nether");
                String portalEnd = node.getOrElse("portal-end", "sky-end");

                WorldConfig wc = new WorldConfig(worldName, envString, portalNether, portalEnd);
                worldConfigs.put(worldName, wc);
            }
        }
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