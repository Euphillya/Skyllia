package fr.euphyllia.skyllia.managers;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.PermissionManager;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.gamerule.GameRuleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.Permissions;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsInventory;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyllia.cache.island.PlayersInIslandCache;
import fr.euphyllia.skyllia.cache.rules.PermissionGameRuleInIslandCache;
import fr.euphyllia.skyllia.cache.rules.PermissionRoleInIslandCache;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionsManagers {

    /**
     * A thread-safe map that holds debug permissions for each player and debug type.
     */
    private static final ConcurrentHashMap<UUID, EnumMap<DebugType, Boolean>> debugPermissions = new ConcurrentHashMap<>();

    /**
     * Sets the debug state for a specific type for a given player.
     *
     * @param player The player whose debug state is being set.
     * @param type   The type of debug to set.
     * @param value  The new debug state (true to enable, false to disable).
     */
    public static void setDebug(UUID player, DebugType type, boolean value) {
        debugPermissions.computeIfAbsent(player, p -> new EnumMap<>(DebugType.class)).put(type, value);
    }

    /**
     * Retrieves the debug state for a specific type for a given player.
     *
     * @param player The player whose debug state is being retrieved.
     * @param type   The type of debug to check.
     * @return The current debug state (true if enabled, false otherwise).
     */
    public static boolean isDebugEnabled(UUID player, DebugType type) {
        return debugPermissions.getOrDefault(player, new EnumMap<>(DebugType.class)).getOrDefault(type, false);
    }

    /**
     * Toggles the debug state for a specific type for a given player.
     * If the current state is true, it will be set to false, and vice versa.
     *
     * @param player The player whose debug state is being toggled.
     * @param type   The type of debug to toggle.
     */
    public static void toggleDebug(UUID player, DebugType type) {
        debugPermissions.computeIfAbsent(player, uuid -> new EnumMap<>(DebugType.class))
                .compute(type, (debugType, value) -> value == null || !value);
    }

    /**
     * Vérifie les permissions d'un joueur pour une action spécifique sur une île.
     *
     * @return true si la permission est refusée, false sinon.
     */
    public static boolean testPermissions(Players executorPlayer, Player player, Island island, Permissions permissions, boolean cached) {
        SkyblockManager manager = Skyllia.getPlugin(Skyllia.class).getInterneAPI().getSkyblockManager();

        if (executorPlayer.getRoleType() == RoleType.OWNER) {
            return true;
        }
        if (executorPlayer.getRoleType() == RoleType.BAN) {
            return false;
        }

        boolean isTrusted = PlayersInIslandCache.playerIsTrustedInIsland(island.getId(), player.getUniqueId());

        PermissionRoleIsland permissionRoleIsland;

        if (isTrusted) {
            permissionRoleIsland = PermissionRoleInIslandCache.getPermissionRoleIsland(
                    island.getId(),
                    RoleType.MEMBER,
                    permissions.getPermissionType()
            );
        } else if (cached) {
            permissionRoleIsland = PermissionRoleInIslandCache.getPermissionRoleIsland(
                    island.getId(),
                    executorPlayer.getRoleType(),
                    permissions.getPermissionType()
            );
        } else {
            permissionRoleIsland = manager.getPermissionIsland(
                    island.getId(),
                    permissions.getPermissionType(),
                    executorPlayer.getRoleType()).join();
        }

        PermissionManager permissionManager = new PermissionManager(permissionRoleIsland.permission());
        boolean hasPermission = permissionManager.hasPermission(permissions);

        if (hasPermission) {
            sendDebugPermissions(player, permissions, permissionRoleIsland, isTrusted, true);
            return true;
        } else {
            sendDebugPermissions(player, permissions, permissionRoleIsland, isTrusted, false);
        }

        // Envoi du message de refus de permission
        ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
        return false;
    }

    public static boolean testGameRule(GameRuleIsland gameRule, Island island) {
        long permissionChecker = PermissionGameRuleInIslandCache.getGameRule(island.getId());
        PermissionManager permissionManager = new PermissionManager(permissionChecker);

        if (permissionManager.hasPermission(gameRule.getPermissionValue())) {
            sendDebugGameRule(gameRule, island, true);
            return true;
        } else {
            sendDebugGameRule(gameRule, island, false);
            return false;
        }
    }

    private static void sendDebugGameRule(GameRuleIsland gameRule, Island island, boolean value) {
        Bukkit.getAsyncScheduler().runNow(Skyllia.getPlugin(Skyllia.class), task -> {
            DebugType debugType = getDebugType(null, gameRule);
            for (Players players : island.getMembersCached()) {
                if (debugType != null && isDebugEnabled(players.getMojangId(), debugType)) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(players.getMojangId());
                    if (offlinePlayer.isOnline() || offlinePlayer.getPlayer() == null) continue;
                    offlinePlayer.getPlayer().sendMessage(
                            "GameRule tested : " + gameRule.name() + "\n" +
                                    "Enabled : " + (value ? "yes" : "no"));
                }
            }
        });
    }

    private static void sendDebugPermissions(Player player,
                                             Permissions permissions,
                                             PermissionRoleIsland permissionRoleIsland,
                                             boolean isTrusted,
                                             boolean value) {
        Bukkit.getAsyncScheduler().runNow(Skyllia.getPlugin(Skyllia.class), task -> {
            DebugType debugType = getDebugType(permissions, null);
            if (debugType != null && isDebugEnabled(player.getUniqueId(), debugType)) {
                player.sendMessage(
                        "Player tested : " + player.getName() + "\n" +
                                "Type : " + permissions.getPermissionType().name() + "\n" +
                                "Permission " + permissions.getName() + " : " + (value ? "yes" : "no") + "\n" +
                                "RoleType : " + permissionRoleIsland.roleType() + "\n" +
                                "Trusted : " + (isTrusted ? "yes" : "no"));
            }
        });
    }

    /**
     * Détermine le type de débogage associé à une permission.
     */
    private static DebugType getDebugType(Permissions permissions, GameRuleIsland gameRuleIsland) {
        if (gameRuleIsland != null) {
            return DebugType.GAME_RULE;
        } else if (permissions instanceof PermissionsIsland) {
            return DebugType.ISLAND_PERMISSION;
        } else if (permissions instanceof PermissionsCommandIsland) {
            return DebugType.COMMANDS_PERMISSION;
        } else if (permissions instanceof PermissionsInventory) {
            return DebugType.INVENTORY_PERMISSION;
        }
        return null;
    }


    /**
     * Represents the various types of debugging that can be managed.
     */
    public enum DebugType {
        GAME_RULE,
        COMMANDS_PERMISSION,
        ISLAND_PERMISSION,
        INVENTORY_PERMISSION
    }
}
