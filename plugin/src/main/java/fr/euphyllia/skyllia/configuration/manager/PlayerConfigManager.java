package fr.euphyllia.skyllia.configuration.manager;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.IndentStyle;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlWriter;
import fr.euphyllia.skyllia.managers.ConfigManager;

public class PlayerConfigManager implements ConfigManager {
    private final CommentedFileConfig config;

    private boolean clearInventoryWhenLeave;
    private boolean clearEnderChestWhenLeave;
    private boolean resetExperienceWhenLeave;
    private boolean clearInventoryWhenKicked;
    private boolean clearEnderChestWhenKicked;
    private boolean resetExperienceWhenKicked;
    private boolean clearInventoryWhenDelete;
    private boolean clearEnderChestWhenDelete;
    private boolean resetExperienceWhenDelete;
    private boolean teleportOwnIslandOnJoin;
    private boolean teleportSpawnIfNoIsland;
    private boolean grantPermissions;
    private boolean allowTeleportation;
    private boolean preserveInventoryOnLogout;
    private boolean changed = false;

    public PlayerConfigManager(CommentedFileConfig config) {
        this.config = config;
        loadConfig();
    }

    @Override
    public void loadConfig() {
        changed = false;

        this.clearInventoryWhenLeave = getOrSetDefault("player.island.leave.clear-inventory", true, Boolean.class);
        this.clearEnderChestWhenLeave = getOrSetDefault("player.island.leave.clear-enderchest", true, Boolean.class);
        this.resetExperienceWhenLeave = getOrSetDefault("player.island.leave.clear-experience", true, Boolean.class);

        this.clearInventoryWhenKicked = getOrSetDefault("player.island.kicked.clear-inventory", false, Boolean.class);
        this.clearEnderChestWhenKicked = getOrSetDefault("player.island.kicked.clear-enderchest", false, Boolean.class);
        this.resetExperienceWhenKicked = getOrSetDefault("player.island.kicked.clear-experience", false, Boolean.class);

        this.clearInventoryWhenDelete = getOrSetDefault("player.island.delete.clear-inventory", true, Boolean.class);
        this.clearEnderChestWhenDelete = getOrSetDefault("player.island.delete.clear-enderchest", true, Boolean.class);
        this.resetExperienceWhenDelete = getOrSetDefault("player.island.delete.clear-experience", true, Boolean.class);

        this.teleportOwnIslandOnJoin = getOrSetDefault("player.join.teleport.own-island", true, Boolean.class);
        this.teleportSpawnIfNoIsland = getOrSetDefault("player.join.teleport.spawn-not-island", false, Boolean.class);

        this.grantPermissions = getOrSetDefault("player.permissions.grant-permissions", true, Boolean.class);
        this.allowTeleportation = getOrSetDefault("player.permissions.allow-teleportation", true, Boolean.class);
        this.preserveInventoryOnLogout = getOrSetDefault("player.inventory.preserve-inventory-on-logout", true, Boolean.class);

        if (changed) {
            TomlWriter tomlWriter = new TomlWriter();
            tomlWriter.setIndent(IndentStyle.NONE);
            tomlWriter.write(config, config.getFile(), WritingMode.REPLACE);
        }
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
            return (T) value; // Bonne instance directement
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

    @Override
    public void reloadFromDisk() {
        config.load();
    }

    public boolean isClearInventoryWhenLeave() {
        return clearInventoryWhenLeave;
    }

    public boolean isClearEnderChestWhenLeave() {
        return clearEnderChestWhenLeave;
    }

    public boolean isResetExperienceWhenLeave() {
        return resetExperienceWhenLeave;
    }

    public boolean isClearInventoryWhenKicked() {
        return clearInventoryWhenKicked;
    }

    public boolean isClearEnderChestWhenKicked() {
        return clearEnderChestWhenKicked;
    }

    public boolean isResetExperienceWhenKicked() {
        return resetExperienceWhenKicked;
    }

    public boolean isClearInventoryWhenDelete() {
        return clearInventoryWhenDelete;
    }

    public boolean isClearEnderChestWhenDelete() {
        return clearEnderChestWhenDelete;
    }

    public boolean isResetExperienceWhenDelete() {
        return resetExperienceWhenDelete;
    }

    public boolean isTeleportOwnIslandOnJoin() {
        return teleportOwnIslandOnJoin;
    }

    public boolean isTeleportSpawnIfNoIsland() {
        return teleportSpawnIfNoIsland;
    }

    public boolean isGrantPermissions() {
        return grantPermissions;
    }

    public boolean isAllowTeleportation() {
        return allowTeleportation;
    }

    public boolean isPreserveInventoryOnLogout() {
        return preserveInventoryOnLogout;
    }
}
