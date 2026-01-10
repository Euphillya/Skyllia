package fr.euphyllia.skyllia.api.database;

import fr.euphyllia.skyllia.api.permissions.CompiledPermissions;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.permissions.PermissionSet;
import fr.euphyllia.skyllia.api.permissions.PermissionSetCodec;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;

import java.util.UUID;

public abstract class IslandPermissionQuery {

    public abstract CompiledPermissions loadCompiled(UUID islandId, PermissionRegistry registry);

    /** DB write only (impl can override if it wants, but default is fine) */
    public boolean set(UUID islandId, RoleType role, PermissionId id, boolean value) {
        return set(islandId, PermissionRegistryHolder.registry(), role, id, value);
    }

    public abstract boolean saveRole(UUID islandId, RoleType role, byte[] wordsBlob);

    public abstract boolean deleteRole(UUID islandId, RoleType role);

    /**
     * Shared logic: load -> flip bit -> save blob
     * (DB-only; no runtime cache update here)
     */
    public final boolean set(UUID islandId, PermissionRegistry registry, RoleType role, PermissionId id, boolean value) {
        CompiledPermissions compiled = loadCompiled(islandId, registry);
        if (compiled == null) return false;

        PermissionSet set = compiled.setFor(role);
        if (set == null) return false;

        set.set(id, value);
        byte[] blob = PermissionSetCodec.encodeLongs(set.snapshotWords());
        return saveRole(islandId, role, blob);
    }

    /**
     * Small indirection to avoid depending on SkylliaAPI in api module (if you want).
     * If you don't care, remove this class and call SkylliaAPI.getPermissionRegistry() directly.
     */
    private static final class PermissionRegistryHolder {
        private static PermissionRegistry registry;
        private PermissionRegistryHolder() {}
        static PermissionRegistry registry() {
            // replace this with SkylliaAPI.getPermissionRegistry() if allowed in this module
            if (registry == null) throw new IllegalStateException("PermissionRegistry not set");
            return registry;
        }
        static void set(PermissionRegistry r) { registry = r; }
    }
}
