package fr.euphyllia.skyllia_papi;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.PermissionManager;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.gamerule.GameRuleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.*;
import fr.euphyllia.skyllia.cache.PermissionGameRuleInIslandCache;
import fr.euphyllia.skyllia.cache.PermissionRoleInIslandCache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

public class PlaceholderProcessor {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    private static final Logger log = LogManager.getLogger(PlaceholderProcessor.class);

    private static final LoadingCache<CacheKey, String> CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build(new CacheLoader<>() {
                @Override
                public @NotNull String load(@NotNull CacheKey cacheKey) {
                    return processIsland(cacheIslandByPlayerId.getUnchecked(cacheKey.playerId()), cacheKey.playerId(), cacheKey.placeholder());
                }
            });

    private static final LoadingCache<UUID, Optional<Island>> cacheIslandByPlayerId = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build(new CacheLoader<>() {
                @Override
                public @NotNull Optional<Island> load(@NotNull UUID playerId) {
                    CompletableFuture<Island> future = SkylliaAPI.getIslandByPlayerId(playerId);
                    if (future == null) {
                        return Optional.empty();
                    }
                    try {
                        return Optional.ofNullable(future.get(5, TimeUnit.SECONDS));
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        log.error(e.getMessage());
                        return Optional.empty();
                    }
                }
            });

    public static String process(UUID playerId, String placeholder) {
        Optional<Island> island = cacheIslandByPlayerId.getUnchecked(playerId);
        if (island.isEmpty()) {
            return "";
        }

        if (placeholder.startsWith("island")) {
            return CACHE.getUnchecked(new CacheKey(playerId, placeholder));
        } else if (placeholder.startsWith("permissions")) {
            return processPermissionsPlaceholder(island.get(), playerId, placeholder);
        } else if (placeholder.startsWith("gamerule")) {
            return processGamerulePlaceholder(island.get(), playerId, placeholder);
        } else if (placeholder.startsWith("ore")) {
            if (Bukkit.getPluginManager().getPlugin("SkylliaOre") != null) {
                return OrePlaceHolder.processOrePlaceholder(island.get(), playerId, placeholder);
            }
        }

        return "Not Supported";
    }

    private static String processIsland(Optional<Island> island, UUID playerId, String placeholder) {
        if (island.isEmpty()) {
            return "";
        }
        if (placeholder.startsWith("island")) {
            return processIslandPlaceholder(island.get(), playerId, placeholder);
        }
        return "";
    }

    private static String processIslandPlaceholder(Island island, UUID playerId, String placeholder) {
        switch (placeholder.toLowerCase(Locale.ROOT)) {
            case "island_size":
                return String.valueOf(island.getSize());
            case "island_members_max_size":
                return String.valueOf(island.getMaxMembers());
            case "island_members_size":
                return String.valueOf(island.getMembers().size());
            case "island_rank":
                return island.getMember(playerId).getRoleType().name();
            case "island_tps":
                Player player = Bukkit.getPlayer(playerId);
                if (player == null || !SkylliaAPI.isWorldSkyblock(player.getWorld())) {
                    return String.valueOf(-1.0);
                }
                double[] tpsValues = SkylliaAPI.getTPS(player.getChunk());
                if (tpsValues == null) {
                    return String.valueOf(-1.0);
                }
                return DECIMAL_FORMAT.format(tpsValues[0]);
            default:
                return "-1";
        }
    }

    private static String processPermissionsPlaceholder(Island island, UUID playerId, String placeholder) {
        String[] split = placeholder.split("_", 4);
        if (split.length < 4) {
            return "Invalid placeholder format";
        }

        String roleTypeRaw = split[1];
        String permissionTypeRaw = split[2];
        String permissionNameRaw = split[3];

        RoleType roleType;
        PermissionsType permissionsType;

        try {
            roleType = RoleType.valueOf(roleTypeRaw.toUpperCase());
            permissionsType = PermissionsType.valueOf(permissionTypeRaw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return "Invalid role or permission type";
        }

        Permissions permissions;
        try {
            switch (permissionsType) {
                case COMMANDS:
                    permissions = PermissionsCommandIsland.valueOf(permissionNameRaw.toUpperCase());
                    break;
                case ISLAND:
                    permissions = PermissionsIsland.valueOf(permissionNameRaw.toUpperCase());
                    break;
                case INVENTORY:
                    permissions = PermissionsInventory.valueOf(permissionNameRaw.toUpperCase());
                    break;
                default:
                    return "Invalid permission type";
            }
        } catch (IllegalArgumentException e) {
            return "Invalid permission name";
        }

        PermissionRoleIsland permissionRoleIsland = PermissionRoleInIslandCache.getPermissionRoleIsland(
                island.getId(), roleType, permissions.getPermissionType());
        PermissionManager permissionManager = new PermissionManager(permissionRoleIsland.permission());
        return String.valueOf(permissionManager.hasPermission(permissions));
    }

    private static String processGamerulePlaceholder(Island island, UUID playerId, String placeholder) {
        String[] split = placeholder.split("_", 2);
        if (split.length < 2) {
            return "Invalid placeholder format";
        }

        String gameRuleRaw = split[1];
        GameRuleIsland gameRuleIsland;

        try {
            gameRuleIsland = GameRuleIsland.valueOf(gameRuleRaw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return "Invalid GameRule";
        }

        long permissionChecker = PermissionGameRuleInIslandCache.getGameruleInIsland(island.getId());
        PermissionManager permissionManager = new PermissionManager(permissionChecker);
        return String.valueOf(permissionManager.hasPermission(gameRuleIsland.getPermissionValue()));
    }



    private record CacheKey(UUID playerId, String placeholder) {}
}
