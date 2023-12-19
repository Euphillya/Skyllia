package fr.euphyllia.skyfolia.api.skyblock.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum PermissionsIsland {
    DEFAULT(0);

    private final int permissionValue;

    PermissionsIsland(int permissionInt) {
        this.permissionValue = permissionInt;
    }

    public static int permissionValue(String names) {
        try {
            return PermissionsIsland.valueOf(names).getPermissionValue();
        } catch (IllegalArgumentException e) {
            return -1;
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

    public int getPermissionValue() {
        return this.permissionValue;
    }
}
