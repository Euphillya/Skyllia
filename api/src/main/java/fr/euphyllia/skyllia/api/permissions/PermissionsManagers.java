package fr.euphyllia.skyllia.api.permissions;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages permission and flag checks for Skyllia islands.
 * <p>
 * Provides methods to verify whether a player holds a given {@link PermissionId}
 * on an island, and whether an island has a specific {@link FlagId} enabled.
 * </p>
 */
public class PermissionsManagers {

    private static final Logger log = LoggerFactory.getLogger(PermissionsManagers.class);

    /**
     * Checks whether the given player has the specified permission on the island.
     * No Bukkit permission bypass is applied and debug logging is disabled.
     *
     * @param player     the player to check.
     * @param island     the island on which the permission is checked.
     * @param permission the permission to verify.
     * @return {@code true} if the player has the permission, {@code false} otherwise.
     */
    public boolean hasPermission(Player player, Island island, PermissionId permission) {
        return hasPermission(player, island, permission, null, false);
    }

    /**
     * Checks whether the given player has the specified permission on the island,
     * with an optional Bukkit permission bypass. Debug logging is disabled.
     *
     * @param player           the player to check.
     * @param island           the island on which the permission is checked.
     * @param permission       the permission to verify.
     * @param bukkitPermission an optional Bukkit permission node; if the player holds it, access is granted immediately.
     * @return {@code true} if the player has the permission, {@code false} otherwise.
     */
    public boolean hasPermission(Player player, Island island, PermissionId permission, @Nullable String bukkitPermission) {
        return hasPermission(player, island, permission, bukkitPermission, false);
    }

    /**
     * Checks whether the given player has the specified permission on the island,
     * with an optional Bukkit permission bypass and optional debug logging.
     * <p>
     * Resolution order:
     * <ol>
     *   <li>If {@code bukkitPermission} is non-null and the player holds it, access is granted immediately.</li>
     *   <li>If the player's role is {@link RoleType#OWNER}, access is always granted.</li>
     *   <li>If the player's role is {@link RoleType#BAN}, access is always denied.</li>
     *   <li>Otherwise, the island's compiled permissions are consulted for the player's role.</li>
     * </ol>
     * </p>
     *
     * @param player           the player to check.
     * @param island           the island on which the permission is checked.
     * @param permission       the permission to verify.
     * @param bukkitPermission an optional Bukkit permission node; if the player holds it, access is granted immediately.
     * @param debug            if {@code true}, detailed resolution steps are logged at INFO level.
     * @return {@code true} if the player has the permission, {@code false} otherwise.
     */
    public boolean hasPermission(Player player, Island island, PermissionId permission, @Nullable String bukkitPermission, boolean debug) {
        if (bukkitPermission != null && player.hasPermission(bukkitPermission)) {
            if (debug) {
                String permName;
                try {
                    permName = SkylliaAPI.getPermissionRegistry().node(permission).node().toString();
                } catch (Exception e) {
                    permName = "unknown#" + permission.index();
                }
                log.info("Player {} has Bukkit permission {}, granting access to {}", player.getName(), bukkitPermission, permName);
            }
            return true;
        }
        var member = island.getMember(player.getUniqueId());
        RoleType role = member != null ? member.getRoleType() : RoleType.VISITOR;
        if (role == null) role = RoleType.VISITOR;

        if (role == RoleType.OWNER) return true;
        if (role == RoleType.BAN) return false;

        var compiled = island.getCompiledPermissions();
        boolean has = compiled.has(SkylliaAPI.getPermissionRegistry(), role, permission);
        if (debug) {
            String permName;
            try {
                permName = SkylliaAPI.getPermissionRegistry().node(permission).node().toString();
            } catch (Exception e) {
                permName = "unknown#" + permission.index();
            }
            log.info("Player {} has role {}, permission {}: {}", player.getName(), role, permName, has);
        }
        return has;
    }

    /**
     * Checks whether the given island has the specified flag enabled.
     *
     * @param island the island to check.
     * @param flag   the flag to verify; if {@code null}, {@code false} is returned immediately.
     * @return {@code true} if the flag is enabled on the island, {@code false} otherwise.
     */
    public boolean hasFlag(Island island, FlagId flag) {
        if (flag == null) return false;
        return island.getIslandFlags()
                .has(SkylliaAPI.getFlagRegistry(), flag);
    }

    /**
     * Checks whether the given island has either the specific flag or the fallback flag enabled.
     *
     * @param island   the island to check.
     * @param specific the primary flag to verify.
     * @param fallback the fallback flag to verify if the primary is not set.
     * @return {@code true} if either flag is enabled on the island, {@code false} otherwise.
     */
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