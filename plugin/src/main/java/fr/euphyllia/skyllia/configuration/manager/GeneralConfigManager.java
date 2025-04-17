package fr.euphyllia.skyllia.configuration.manager;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fr.euphyllia.skyllia.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class GeneralConfigManager implements ConfigManager {

    private final CommentedFileConfig config;
    private int configVersion;
    private boolean verbose;
    private int updateCacheTimer;
    private boolean preventDeletionIfHasMembers;
    private int regionDistance;
    private int maxIslands;
    private boolean deleteChunkPerimeterIsland;
    private boolean spawnEnabled;
    private String spawnWorld;
    private double spawnX;
    private double spawnY;
    private double spawnZ;
    private float spawnYaw;
    private float spawnPitch;
    private boolean debugPermission;

    public GeneralConfigManager(CommentedFileConfig config) {
        this.config = config;
        loadConfig();
    }

    @Override
    public void loadConfig() {
        this.configVersion = config.getOrElse("config-version", 4);
        this.verbose = config.getOrElse("verbose", false);
        this.updateCacheTimer = config.getOrElse("settings.global.cache.update-timer-seconds", 30);
        this.regionDistance = config.getOrElse("settings.island.region-distance", -1);
        this.maxIslands = config.getOrElse("settings.island.max-islands", 500_000);

        this.preventDeletionIfHasMembers = config.getOrElse("settings.island.delete.prevent-deletion-if-has-members", true);
        this.deleteChunkPerimeterIsland = config.getOrElse("settings.island.delete.chunk-perimeter-island", false);

        this.spawnEnabled = config.getOrElse("settings.spawn.enable", true);
        this.spawnWorld = config.getOrElse("settings.spawn.world-name", "world");
        this.spawnX = config.getOrElse("settings.spawn.block-x", 0.0);
        this.spawnY = config.getOrElse("settings.spawn.block-y", 64.0);
        this.spawnZ = config.getOrElse("settings.spawn.block-z", 0.0);
        this.spawnYaw = (float) (double) config.getOrElse("settings.spawn.yaw", 0.0);
        this.spawnPitch = (float) (double) config.getOrElse("settings.spawn.pitch", 0.0);

        this.debugPermission = config.getOrElse("debug.permission", false);
    }

    public int getConfigVersion() {
        return configVersion;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public int getUpdateCacheTimer() {
        return updateCacheTimer;
    }

    public boolean isPreventDeletionIfHasMembers() {
        return preventDeletionIfHasMembers;
    }

    public boolean isSpawnEnabled() {
        return spawnEnabled;
    }

    public String getSpawnWorld() {
        return spawnWorld;
    }

    public double getSpawnX() {
        return spawnX;
    }

    public double getSpawnY() {
        return spawnY;
    }

    public double getSpawnZ() {
        return spawnZ;
    }

    public float getSpawnYaw() {
        return spawnYaw;
    }

    public float getSpawnPitch() {
        return spawnPitch;
    }

    public boolean isDebugPermission() {
        return debugPermission;
    }

    public Location getSpawnLocation() {
        return spawnEnabled ? new Location(Bukkit.getWorld(spawnWorld), spawnX, spawnY, spawnZ, spawnYaw, spawnPitch) : null;
    }

    public int getRegionDistance() {
        return regionDistance;
    }

    public int getMaxIslands() {
        return maxIslands;
    }

    public boolean isDeleteChunkPerimeterIsland() {
        return deleteChunkPerimeterIsland;
    }
}
