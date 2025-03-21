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
     *   "sky-overworld" -> (Environnements.NORMAL, "sky-nether", "sky-end")
     */
    private final Map<String, WorldConfig> worldConfigs = new HashMap<>();

    private boolean suppressWarnNetherEndWorld = false;

    private final CommentedFileConfig config;
    public WorldConfigManager(CommentedFileConfig config) {
        this.config = config;
        loadConfig();
    }

    @Override
    public void loadConfig() {
        this.suppressWarnNetherEndWorld = config.getOrElse("player.island.leave.clear-inventory", suppressWarnNetherEndWorld);

        worldConfigs.clear();

        Map<String, Object> worlds = config.getOrElse("worlds", new HashMap<>());
        for (String worldName : worlds.keySet()) {
            CommentedConfig node = config.get("worlds." + worldName);
            if (node == null) continue;

            String envString     = node.getOrElse("environment", "NORMAL");
            String portalNether  = node.getOrElse("portal-nether", "sky-nether");
            String portalEnd     = node.getOrElse("portal-end", "sky-end");

            WorldConfig wc = new WorldConfig(worldName, envString, portalNether, portalEnd);
            worldConfigs.put(worldName, wc);
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