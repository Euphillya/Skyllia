package fr.euphyllia.skyllia.api.skyblock.model.gamerule;

import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsIsland;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum GameRuleIsland {
    SPAWN_HOSTILE(1),
    SPAWN_PASSIVE(2),
    HUMAN_EXPLOSION(4),
    MOB_EXPLOSION(8),
    ENDERMAN_PICK_BLOCK(16),
    ZOMBIE_BREAK_DOORS(32),
    FIRE_SPREADING(64),
    ;

    private final long permissionValue;

    GameRuleIsland(long permissionLong) {
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

    public long getPermissionValue() {
        return this.permissionValue;
    }
}
