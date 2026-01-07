package fr.euphyllia.skyllia.api.permissions;

import fr.euphyllia.skyllia.api.skyblock.model.RoleType;

import java.util.EnumMap;

public final class CompiledPermissions {
    private int version;
    private final EnumMap<RoleType, PermissionSet> byRole = new EnumMap<>(RoleType.class);

    public CompiledPermissions(PermissionRegistry registry) {
        int size = registry.size();
        for (RoleType role : RoleType.values()) {
            byRole.put(role, new PermissionSet(size));
        }
        this.version = registry.version();
    }

    public void ensureUpToDate(PermissionRegistry registry) {
        int regVersion = registry.version();
        if (this.version == regVersion) return;

        synchronized (this) {
            if (this.version == regVersion) return;

            int newSize = registry.size();
            for (PermissionSet set : byRole.values()) {
                set.ensureCapacity(newSize);
            }
            this.version = regVersion;
        }
    }


    public boolean has(PermissionRegistry registry, RoleType role, PermissionId id) {
        ensureUpToDate(registry);
        return byRole.get(role).has(id);
    }
}
