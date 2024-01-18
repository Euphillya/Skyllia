package fr.euphyllia.skyllia.api.skyblock.model.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum PermissionsInventory implements Permissions {
    DEFAULT(0),
    OPEN_CHEST(1),
    OPEN_ANVIL(2),
    OPEN_WORKBENCH(4),
    OPEN_ENCHANTING(8),
    OPEN_BREWING(16),
    OPEN_SMITHING(32),
    OPEN_BEACON(64),
    OPEN_SHULKER_BOX(128),
    OPEN_FURNACE(256),
    OPEN_LECTERN(512),
    OPEN_CRAFTER(1_024), // 1.20.4
    OPEN_LOOM(2_048),
    OPEN_GRINDSTONE(4_096),
    OPEN_STONECUTTER(8_192),
    OPEN_CARTOGRAPHY(16_384),
    OPEN_MERCHANT(32_768),
    OPEN_HOPPER(65_536),
    OPEN_BARREL(131_072),
    OPEN_BLAST_FURNACE(262_144),
    OPEN_SMOKER(524_288),
    OPEN_SMITHING_NEW(1_048_576),
    OPEN_DISPENSER(2_097_152),
    OPEN_DROPPER(4_194_304),
    ;

    private final long permissionValue;

    PermissionsInventory(long permissionLong) {
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
        return PermissionsType.INVENTORY;
    }
}
