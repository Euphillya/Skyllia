package fr.euphyllia.skyllia.api.skyblock.model.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum PermissionsIsland implements Permissions {
    DEFAULT(0),
    BLOCK_BREAK(1),
    BLOCK_PLACE(2),
    BUCKETS(4),
    REDSTONE(8),
    PVP(16),
    KILL_MONSTER(32),
    KILL_ANIMAL(64),
    DROP_ITEMS(128),
    PICKUP_ITEMS(256),
    USE_NETHER_PORTAL(512),
    USE_END_PORTAL(1_024),
    INTERACT_ENTITIES(2_048),
    KILL_UNKNOWN_ENTITY(4_096),
    KILL_NPC(8_192),
    ;

    private final long permissionValue;

    PermissionsIsland(long permissionLong) {
        this.permissionValue = permissionLong;
    }

    public static long permissionValue(String names) {
        try {
            return PermissionsIsland.valueOf(names).getPermissionValue();
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    public static long maxPermissionsValue() {
        return Arrays.stream(PermissionsIsland.values()).mapToLong(PermissionsIsland::getPermissionValue).sum();
    }

    public static List<String> getListPermissions() {
        List<String> list = new ArrayList<>();
        for (PermissionsIsland permissionsIsland : PermissionsIsland.values()) {
            String name = permissionsIsland.name();
            list.add(name);
        }
        return list;
    }

    @Override
    public long getPermissionValue() {
        return this.permissionValue;
    }

    @Override
    public PermissionsType getPermissionType() {
        return PermissionsType.ISLAND;
    }
}
