package fr.euphyllia.skyllia.api.permissions;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class PermissionsManagers {


    public boolean hasPermission(Player player, Island island, PermissionId permission) {
        return hasPermission(player, island, permission, null);
    }

    public boolean hasPermission(Player player, Island island, PermissionId permission, @Nullable String bukkitPermission) {
        if (bukkitPermission != null && player.hasPermission(bukkitPermission)) {
            return true;
        }
        var member = island.getMember(player.getUniqueId());
        RoleType role = member != null ? member.getRoleType() : RoleType.VISITOR;
        if (role == null) role = RoleType.VISITOR;

        if (role == RoleType.OWNER) return true;
        if (role == RoleType.BAN) return false;

        var compiled = island.getCompiledPermissions();
        return compiled.has(SkylliaAPI.getPermissionRegistry(), role, permission);
    }

    public boolean hasFlag(Island island, FlagId flag) {
        if (flag == null) return false;
        return island.getIslandFlags()
                .has(SkylliaAPI.getFlagRegistry(), flag);
    }

    public boolean hasFlag(Island island, FlagId specific, FlagId fallback) {
        var flags = island.getIslandFlags();
        var registry = SkylliaAPI.getFlagRegistry();
        return flags.has(registry, specific) || flags.has(registry, fallback);
    }

    /**
     * @deprecated Use {@link #hasFlag(Island, FlagId)} instead.
     */
    @Deprecated(forRemoval = true)
    public boolean hasIslandFlag(Island island, PermissionId flag) {
        throw new UnsupportedOperationException(
                "hasIslandFlag(PermissionId) is removed. Use hasFlag(FlagId) with the FlagRegistry.");
    }

    /**
     * @deprecated Use {@link #hasFlag(Island, FlagId, FlagId)} instead.
     */
    @Deprecated(forRemoval = true)
    public boolean hasIslandFlag(Island island, PermissionId specific, PermissionId fallback) {
        throw new UnsupportedOperationException(
                "hasIslandFlag(PermissionId, PermissionId) is removed. Use hasFlag(FlagId, FlagId) with the FlagRegistry.");
    }


}
