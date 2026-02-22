package fr.euphyllia.skyllia.api.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldConfig {
    private static final Logger log = LogManager.getLogger(WorldConfig.class);

    private final String worldName;
    private final World.Environment environment;
    private final String portalNether;
    private final String portalEnd;
    private final String generator;
    private @Nullable Integer seaHeight;
    private @Nullable String seaBlock;
    private boolean deleteIsland = false;

    private final @Nullable String biomeId;

    public WorldConfig(String worldName, String environmentStr, String portalNether, String portalEnd, String generator, String biomeId, boolean deleteIsland) {
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
        this.generator = generator;

        this.biomeId = (biomeId == null || biomeId.isBlank()) ? null : biomeId;
        this.deleteIsland = deleteIsland;
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

    public String getGenerator() {
        return generator;
    }

    public @Nullable String getBiomeId() {
        return biomeId;
    }

    public @Nullable Integer getSeaHeight() {
        return seaHeight;
    }

    public void setSeaHeight(@NotNull Integer seaHeight) {
        this.seaHeight = seaHeight;
    }

    public @Nullable String getSeaBlock() {
        return seaBlock;
    }

    public void setSeaBlock(@NotNull String seaBlock) {
        this.seaBlock = seaBlock;
    }

    public boolean shouldDeleteIsland() {
        return deleteIsland;
    }

    public void setDeleteIsland(boolean deleteIsland) {
        this.deleteIsland = deleteIsland;
    }
}