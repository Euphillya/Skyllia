package fr.euphyllia.skyfolia.api.skyblock.model.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum PermissionsCommandIsland implements Permissions {
    DEFAULT(0),
    DEMOTE(1),
    PROMOTE(2),
    KICK(4),
    ACCESS(8);

    private final int permissionValue;

    PermissionsCommandIsland(int permissionInt) {
        this.permissionValue = permissionInt;
    }

    public static int permissionValue(String names) {
        try {
            return PermissionsIsland.valueOf(names).getPermissionValue();
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    public static int maxPermissionsValue() {
        return Arrays.stream(PermissionsIsland.values()).mapToInt(PermissionsIsland::getPermissionValue).sum();
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
    public int getPermissionValue() {
        return this.permissionValue;
    }
}
