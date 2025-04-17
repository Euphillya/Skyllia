package fr.euphyllia.skyllia.configuration.manager;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
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

    public PlayerConfigManager(CommentedFileConfig config) {
        this.config = config;
        loadConfig();
    }

    @Override
    public void loadConfig() {
        this.clearInventoryWhenLeave = config.getOrElse("player.island.leave.clear-inventory", true);
        this.clearEnderChestWhenLeave = config.getOrElse("player.island.leave.clear-enderchest", true);
        this.resetExperienceWhenLeave = config.getOrElse("player.island.leave.clear-experience", true);

        this.clearInventoryWhenKicked = config.getOrElse("player.island.kicked.clear-inventory", false);
        this.clearEnderChestWhenKicked = config.getOrElse("player.island.kicked.clear-enderchest", false);
        this.resetExperienceWhenKicked = config.getOrElse("player.island.kicked.clear-experience", false);

        this.clearInventoryWhenDelete = config.getOrElse("player.island.delete.clear-inventory", true);
        this.clearEnderChestWhenDelete = config.getOrElse("player.island.delete.clear-enderchest", true);
        this.resetExperienceWhenDelete = config.getOrElse("player.island.delete.clear-experience", true);

        this.teleportOwnIslandOnJoin = config.getOrElse("player.join.teleport.own-island", true);
        this.teleportSpawnIfNoIsland = config.getOrElse("player.join.teleport.spawn-not-island", false);

        this.grantPermissions = config.getOrElse("player.permissions.grant-permissions", true);
        this.allowTeleportation = config.getOrElse("player.permissions.allow-teleportation", true);
        this.preserveInventoryOnLogout = config.getOrElse("player.inventory.preserve-inventory-on-logout", true);
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
