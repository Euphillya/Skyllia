package fr.euphyllia.skyllia.configuration.manager;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fr.euphyllia.skyllia.managers.ConfigManager;

import java.util.HashMap;
import java.util.Map;

public class WorldConfigManager implements ConfigManager {


    private final Map<String, Environnements> worldConfigs = new HashMap<>();
    private boolean suppressWarnNetherEndWorld = false;

    private final CommentedFileConfig config;
    public WorldConfigManager(CommentedFileConfig config) {
        this.config = config;
        loadConfig();
    }

    @Override
    public void loadConfig() {
        for (Object obj : config.getOrElse("worlds", new HashMap<>()).keySet()) {
            String worldName = (String) obj;
            String environment = config.getOrElse("worlds." + worldName + ".environment", Environnements.NORMAL.name());
            worldConfigs.put(worldName, Environnements.valueOf(environment));
        }
        suppressWarnNetherEndWorld = config.getOrElse("player.island.leave.clear-inventory", suppressWarnNetherEndWorld);
    }

    public Environnements getWorldEnvironment(String worldName) {
        return worldConfigs.get(worldName);
    }

    public Map<String, Environnements> getWorldConfigs() {
        return worldConfigs;
    }

    public boolean isSuppressWarnNetherEndWorld() {
        return suppressWarnNetherEndWorld;
    }

    public enum Environnements {
        NORMAL, NETHER, THE_END
    }
}