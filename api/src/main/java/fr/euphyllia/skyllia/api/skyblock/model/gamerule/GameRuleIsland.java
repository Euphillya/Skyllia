package fr.euphyllia.skyllia.api.skyblock.model.gamerule;

import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsIsland;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents various game rules that can be applied to a Skyblock island.
 */
public enum GameRuleIsland {
    /**
     * Disables the spawning of hostile mobs on the island.
     */
    DISABLE_SPAWN_HOSTILE(1),

    /**
     * Disables the spawning of passive mobs on the island.
     */
    DISABLE_SPAWN_PASSIVE(2),

    /**
     * Disables the spawning of unknown mobs on the island.
     */
    DISABLE_SPAWN_UNKNOWN(4),

    /**
     * Disables explosions caused by players on the island.
     */
    DISABLE_HUMAN_EXPLOSION(8),

    /**
     * Disables explosions caused by mobs on the island.
     */
    DISABLE_MOB_EXPLOSION(16),

    /**
     * Disables Endermen from picking up blocks on the island.
     */
    DISABLE_ENDERMAN_PICK_BLOCK(32),

    /**
     * Disables fire spreading on the island.
     */
    DISABLE_FIRE_SPREADING(64),

    /**
     * Disables mobs from picking up items on the island.
     */
    DISABLE_MOB_PICKUP_ITEMS(128),

    /**
     * Disables unknown explosions on the island.
     */
    DISABLE_UNKNOWN_EXPLOSION(256),

    /**
     * Disables passive mob griefing on the island.
     */
    DISABLE_PASSIF_MOB_GRIEFING(512),

    /**
     * Disables hostile mob griefing on the island.
     */
    DISABLE_HOSTILE_MOB_GRIEFING(1_024),

    /**
     * Disables unknown mob griefing on the island.
     */
    DISABLE_UNKNOWN_MOB_GRIEFING(2_048),

    /**
     * Disables player griefing on the island.
     */
    DISABLE_PLAYER_GRIEFING(4_096);

    private final long permissionValue;

    /**
     * Constructs a GameRuleIsland with the specified permission value.
     *
     * @param permissionLong The permission value.
     */
    GameRuleIsland(long permissionLong) {
        this.permissionValue = permissionLong;
    }

    /**
     * Converts a string representation of permission names to a long value.
     *
     * @param names The string representation of permission names.
     * @return The long value of the permissions.
     */
    public static long permissionValue(String names) {
        try {
            return PermissionsIsland.valueOf(names).getPermissionValue();
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    /**
     * Gets the maximum value of all permissions combined.
     *
     * @return The maximum permissions value.
     */
    public static long maxPermissionsValue() {
        return Arrays.stream(PermissionsIsland.values()).mapToLong(PermissionsIsland::getPermissionValue).sum();
    }

    /**
     * Gets a list of all permission names.
     *
     * @return A list of permission names.
     */
    public static List<String> getListPermissions() {
        List<String> list = new ArrayList<>();
        for (PermissionsIsland permissionsIsland : PermissionsIsland.values()) {
            String name = permissionsIsland.name();
            list.add(name);
        }
        return list;
    }

    /**
     * Gets the permission value of this game rule.
     *
     * @return The permission value.
     */
    public long getPermissionValue() {
        return this.permissionValue;
    }
}
