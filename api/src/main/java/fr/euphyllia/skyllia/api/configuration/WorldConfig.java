package fr.euphyllia.skyllia.api.configuration;

import fr.euphyllia.skyllia.api.world.SkylliaEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldConfig {
    private static final Logger log = LogManager.getLogger(WorldConfig.class);
    private final String worldName;
    private final SkylliaEnvironment environment;
    private final String portalNether;
    private final String portalEnd;
    private final String generator;

    public WorldConfig(String worldName, String environmentStr, String portalNether, String portalEnd, String generator) {
        SkylliaEnvironment env;
        try {
            env = SkylliaEnvironment.valueOf(environmentStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Environment {} does not exist. Using the default NORMAL World.", environmentStr.toUpperCase(), e);
            env = SkylliaEnvironment.NORMAL;
        }
        this.worldName = worldName;
        this.environment = env;
        this.portalNether = portalNether;
        this.portalEnd = portalEnd;
        this.generator = generator;
    }

    public SkylliaEnvironment getEnvironment() {
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

    public String getGenerator() {
        return generator;
    }

    @Override
    public String toString() {
        return "{class=WorldConfig, worldName=" + getWorldName() + ", environment=" + environment.name() + ", portalNether=" + getPortalNether() + ", portalEnd=" + getPortalEnd() + "}";
    }
}