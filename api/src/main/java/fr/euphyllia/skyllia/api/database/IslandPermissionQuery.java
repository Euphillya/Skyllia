package fr.euphyllia.skyllia.api.database;

import fr.euphyllia.skyllia.api.permissions.CompiledPermissions;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.permissions.PermissionSetCodec;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;

import java.util.UUID;

public abstract class IslandPermissionQuery {

    public abstract CompiledPermissions loadCompiled(UUID islandId, PermissionRegistry registry);

    public abstract boolean set(UUID islandId, RoleType role, PermissionId id, boolean value);

    public abstract boolean saveRole(UUID islandId, RoleType role, byte[] wordsBlob);

    public abstract boolean deleteRole(UUID islandId, RoleType role);

    public boolean set(UUID islandId, PermissionRegistry registry, RoleType role, PermissionId id, boolean value) {
        CompiledPermissions compiled = loadCompiled(islandId, registry);
        compiled.setFor(role).set(id, value);
        byte[] blob = PermissionSetCodec.encodeLongs(compiled.setFor(role).snapshotWords());
        return saveRole(islandId, role, blob);
    }
}
