package fr.euphyllia.skyllia.api.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.World;

public class WorldConfig {
    private static final Logger log = LogManager.getLogger(WorldConfig.class);
    private final String worldName;
    private final World.Environment environment;
    private final String portalNether;
    private final String portalEnd;

    public WorldConfig(String worldName, String environmentStr, String portalNether, String portalEnd) {
        World.Environment env;
        try {
            env = World.Environment.valueOf(environmentStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Environment {} does not exist. Using the default NORMAL World.", environmentStr.toUpperCase(), e);
            env = World.Environment.NORMAL;
        }
        this.worldName = worldName;
        this.environment = env;
        this.portalNether = portalNether;
        this.portalEnd = portalEnd;
    }

    public World.Environment getEnvironment() {
        return environment;
    }

    public String getPortalNether() {
        return portalNether;
    }

    public String getPortalEnd() {
        return portalEnd;
    }

    public String getWorldName() {
        return worldName;
    }
}