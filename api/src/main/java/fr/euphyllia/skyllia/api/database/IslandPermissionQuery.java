package fr.euphyllia.skyllia.api.database;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.permissions.*;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

public abstract class IslandPermissionQuery {

    public abstract CompiledPermissions loadCompiled(UUID islandId, PermissionRegistry registry);

    /**
     * Load the island-wide flags for a given island.
     * Returns {@code null} if no flags are stored yet (caller will use an empty {@link IslandFlags}).
     */
    @Deprecated(forRemoval = true, since = "3.x")
    @ApiStatus.ScheduledForRemoval(inVersion = "4.x")
    public IslandFlags loadIslandFlags(UUID islandId, IslandFlagRegistry registry) {
        return this.loadIslandFlags(islandId, registry, SkylliaAPI.getRegisteredWorlds().getFirst().getWorldName());
    }

    public abstract IslandFlags loadIslandFlags(UUID islandId, IslandFlagRegistry registry, String worldName);

    /**
     * Persist the full flag bitset for an island.
     */
    @Deprecated(forRemoval = true, since = "3.x")
    @ApiStatus.ScheduledForRemoval(inVersion = "4.x")
    public boolean saveIslandFlags(UUID islandId, byte[] wordsBlob) {
        for (String world : SkylliaAPI.getRegisteredWorlds().stream().map(WorldConfig::getWorldName).toList()) {
            if (!saveIslandFlags(islandId, wordsBlob, world)) {
                return false;
            }
        }
        return true;
    }

    public abstract boolean saveIslandFlags(UUID islandId, byte[] wordsBlob, String worldName);

    /**
     * Convenience: flip a single flag and persist it.
     */
    public final boolean setFlag(UUID islandId, IslandFlagRegistry registry, FlagId id, boolean value) {
        for (String world : SkylliaAPI.getRegisteredWorlds().stream().map(WorldConfig::getWorldName).toList()) {
            if (!setFlag(islandId, registry, id, world, value)) {
                return false;
            }
        }
        return true;
    }

    public final boolean setFlag(UUID islandId, IslandFlagRegistry registry, FlagId id, String worldName, boolean value) {
        IslandFlags flags = loadIslandFlags(islandId, registry, worldName);
        if (flags == null) flags = new IslandFlags(registry);

        flags.set(registry, id, value);
        byte[] blob = PermissionSetCodec.encodeLongs(flags.snapshotWords());
        return saveIslandFlags(islandId, blob, worldName);
    }

    /**
     * DB write only (impl can override if it wants, but default is fine)
     */
    public boolean set(UUID islandId, RoleType role, PermissionId id, boolean value) {
        return set(islandId, SkylliaAPI.getPermissionRegistry(), role, id, value);
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
}
