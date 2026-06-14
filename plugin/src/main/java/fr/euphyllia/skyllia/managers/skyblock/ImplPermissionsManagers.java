package fr.euphyllia.skyllia.managers.skyblock;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.FlagId;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionsManagers;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImplPermissionsManagers implements PermissionsManagers {

    private static final Logger log = LoggerFactory.getLogger(ImplPermissionsManagers.class);

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
        return hasPermission(player, island, permission, null, ConfigLoader.general.getDebugSettings().permission());
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
        return hasPermission(player, island, permission, bukkitPermission, ConfigLoader.general.getDebugSettings().permission());
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
        if (bukkitPermission != null && PlayerUtils.hasPermission(player, bukkitPermission)) {
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

        if (!ConfigLoader.general.getPermissionsSettings().checkOwner()) {
            if (role == RoleType.OWNER) return true;
        }
        if (!ConfigLoader.general.getPermissionsSettings().checkBan()) {
            if (role == RoleType.BAN) return false;
        }

        if (SkylliaAPI.getTrustService().isTrusted(island.getId(), player.getUniqueId())) {
            role = RoleType.MEMBER;
        }

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

    @Override
    public boolean hasFlag(Island island, FlagId flag, String worldName) {
        if (flag == null) return false;
        return island.getIslandFlags(worldName)
                .has(SkylliaAPI.getFlagRegistry(), flag);
    }

    @Override
    public boolean hasFlag(Island island, FlagId specific, FlagId fallback, String worldName) {
        var flags = island.getIslandFlags(worldName);
        var registry = SkylliaAPI.getFlagRegistry();
        return flags.has(registry, specific) || flags.has(registry, fallback);
    }
}
