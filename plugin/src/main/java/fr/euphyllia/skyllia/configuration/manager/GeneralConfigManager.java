package fr.euphyllia.skyllia.configuration.manager;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.IndentStyle;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlWriter;
import fr.euphyllia.skyllia.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class GeneralConfigManager implements ConfigManager {

    private final CommentedFileConfig config;
    // Configuration basic
    private int configVersion;
    private boolean verbose;
    // Settings
    private int updateCacheTimer;
    // Island settings
    private int regionDistance;
    private int maxIslands;
    private boolean teleportOutsideIsland;
    private boolean restrictPlayerMovement;
    // Island deletion settings
    private boolean preventDeletionIfHasMembers;
    private boolean deleteChunkPerimeterIsland;
    // Island invitation settings
    private boolean teleportWhenAcceptingInvitation;
    // Spawn settings
    private boolean spawnEnabled;
    private String spawnWorld;
    private double spawnX;
    private double spawnY;
    private double spawnZ;
    private float spawnYaw;
    private float spawnPitch;
    // Debug settings
    private boolean debugPermission;
    private boolean changed = false;

    public GeneralConfigManager(CommentedFileConfig config) {
        this.config = config;
        loadConfig();
    }

    @Override
    public void loadConfig() {
        changed = false;
        this.configVersion = getOrSetDefault("config-version", 4, Integer.class);
        this.verbose = getOrSetDefault("verbose", false, Boolean.class);

        this.updateCacheTimer = getOrSetDefault("settings.global.cache.update-timer-seconds", 30, Integer.class);

        this.regionDistance = getOrSetDefault("settings.island.region-distance", -1, Integer.class);
        this.maxIslands = getOrSetDefault("settings.island.max-islands", 500_000, Integer.class);
        this.teleportOutsideIsland = getOrSetDefault("settings.island.teleport-outside-island", false, Boolean.class);
        this.restrictPlayerMovement = getOrSetDefault("settings.island.restrict-player-movement", false, Boolean.class);

        this.preventDeletionIfHasMembers = getOrSetDefault("settings.island.delete.prevent-deletion-if-has-members", true, Boolean.class);
        this.deleteChunkPerimeterIsland = getOrSetDefault("settings.island.delete.chunk-perimeter-island", false, Boolean.class);

        this.teleportWhenAcceptingInvitation = getOrSetDefault("settings.island.invitation.teleport-when-accepting-invitation", true, Boolean.class);

        this.spawnEnabled = getOrSetDefault("settings.spawn.enable", true, Boolean.class);
        this.spawnWorld = getOrSetDefault("settings.spawn.world-name", "world", String.class);
        this.spawnX = getOrSetDefault("settings.spawn.block-x", 0.0, Double.class);
        this.spawnY = getOrSetDefault("settings.spawn.block-y", 64.0, Double.class);
        this.spawnZ = getOrSetDefault("settings.spawn.block-z", 0.0, Double.class);
        this.spawnYaw = getOrSetDefault("settings.spawn.yaw", 0.0f, Float.class);
        this.spawnPitch = getOrSetDefault("settings.spawn.pitch", 0.0f, Float.class);

        this.debugPermission = getOrSetDefault("debug.permission", false, Boolean.class);

        if (changed) {
            TomlWriter tomlWriter = new TomlWriter();
            tomlWriter.setIndent(IndentStyle.NONE);
            tomlWriter.write(config, config.getFile(), WritingMode.REPLACE);
        }
    }

    @Override
    public void reloadFromDisk() {
        config.load();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOrSetDefault(String path, T defaultValue, Class<T> expectedClass) {
        Object value = config.get(path);
        if (value == null) {
            config.set(path, defaultValue);
            changed = true;
            return defaultValue;
        }

        if (expectedClass.isInstance(value)) {
            return (T) value;
        }

        // Cas spécial : Integer → Long
        if (expectedClass == Long.class && value instanceof Integer) {
            return (T) Long.valueOf((Integer) value);
        }

        // Cas spécial : Double → Float
        if (expectedClass == Float.class && value instanceof Double) {
            return (T) Float.valueOf(((Double) value).floatValue());
        }

        throw new IllegalStateException("Cannot convert value at path '" + path + "' from " + value.getClass().getSimpleName() + " to " + expectedClass.getSimpleName());
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

    public boolean isTeleportOutsideIsland() {
        return teleportOutsideIsland;
    }

    public boolean isTeleportWhenAcceptingInvitation() {
        return teleportWhenAcceptingInvitation;
    }

    public boolean isRestrictPlayerMovement() {
        return restrictPlayerMovement;
    }
}
