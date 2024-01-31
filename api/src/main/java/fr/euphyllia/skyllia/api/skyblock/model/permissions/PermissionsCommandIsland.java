package fr.euphyllia.skyllia.api.skyblock.model.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum PermissionsCommandIsland implements Permissions {
    DEFAULT(0),
    DEMOTE(1),
    PROMOTE(2),
    KICK(4),
    ACCESS(8),
    SET_HOME(16),
    INVITE(32),
    SET_BIOME(64),
    SET_WARP(128),
    DEL_WARP(256),
    TP_WARP(512),
    EXPEL(1_024),
    MANAGE_PERMISSION(2_048),
    BAN(4_096),
    UNBAN(8_192),
    MANAGE_TRUST(16_384),
    MANAGE_GAMERULE(32_768);


    private final long permissionValue;

    PermissionsCommandIsland(long permissionLong) {
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
        return PermissionsType.COMMANDS;
    }
}
