package fr.euphyllia.skyllia_papi;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
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

    private static final Logger LOGGER = LogManager.getLogger(PlaceholderProcessor.class);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    /**
     * Cache pour associer un UUID de joueur à son île.
     * — ExpireAfterWrite(5, SECONDS) : supprime du cache 5s après l’écriture.
     * — RefreshAfterWrite(3, SECONDS) : tente un rafraîchissement en arrière-plan après 3s.
     */
    private static final LoadingCache<UUID, Optional<Island>> ISLAND_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .refreshAfterWrite(3, TimeUnit.SECONDS)
            .build(new CacheLoader<>() {
                @Override
                public @NotNull Optional<Island> load(@NotNull UUID playerId) {
                    return loadIslandByUUID(playerId);
                }

                // Gestion du rafraîchissement en arrière-plan
                @Override
                public @NotNull CompletableFuture<Optional<Island>> asyncReload(
                        @NotNull UUID playerId,
                        @NotNull Optional<Island> oldValue,
                        @NotNull Executor executor
                ) {
                    return CompletableFuture.supplyAsync(() -> loadIslandByUUID(playerId), executor);
                }
            });

    /**
     * Cache pour stocker le résultat final d'un placeholder (String) en fonction du playerId et du placeholder.
     * On utilise la même logique d'expiration + refresh.
     */
    private static final LoadingCache<CacheKey, String> PLACEHOLDER_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .refreshAfterWrite(3, TimeUnit.SECONDS)
            .build(new CacheLoader<>() {
                @Override
                public @NotNull String load(@NotNull CacheKey key) {
                    Optional<Island> islandOpt = Optional.ofNullable(ISLAND_CACHE.get(key.playerId()))
                            .orElse(Optional.empty());
                    return processIsland(islandOpt, key.playerId(), key.placeholder());
                }

                @Override
                public @NotNull CompletableFuture<String> asyncReload(
                        @NotNull CacheKey key,
                        @NotNull String oldValue,
                        @NotNull Executor executor
                ) {
                    return CompletableFuture.supplyAsync(() -> {
                        Optional<Island> islandOpt = Optional.ofNullable(ISLAND_CACHE.get(key.playerId()))
                                .orElse(Optional.empty());
                        return processIsland(islandOpt, key.playerId(), key.placeholder());
                    }, executor);
                }
            });

    /**
     * Point d'entrée pour récupérer un placeholder.
     *
     * @param playerId    UUID du joueur
     * @param placeholder Le placeholder (ex : "island_size", "permissions_modif_command", etc.)
     * @return La valeur du placeholder
     */
    public static String process(UUID playerId, String placeholder) {
        // On vérifie rapidement si le joueur a une île
        Optional<Island> islandOpt = Optional.ofNullable(ISLAND_CACHE.get(playerId))
                .orElse(Optional.empty());
        if (islandOpt.isEmpty()) {
            return "";
        }

        // On détermine quel type de placeholder on traite
        if (placeholder.startsWith("island")) {
            // Cache plus ciblé sur l'info de l'île
            return PLACEHOLDER_CACHE.get(new CacheKey(playerId, placeholder));
        } else if (placeholder.startsWith("permissions")) {
            return processPermissionsPlaceholder(islandOpt.get(), playerId, placeholder);
        } else if (placeholder.startsWith("gamerule")) {
            return processGamerulePlaceholder(islandOpt.get(), placeholder);
        } else if (placeholder.startsWith("ore")) {
            // Si le plugin "SkylliaOre" est présent, on délègue au placeholder dédié
            if (Bukkit.getPluginManager().getPlugin("SkylliaOre") != null) {
                return OrePlaceHolder.processOrePlaceholder(islandOpt.get(), playerId, placeholder);
            }
        }

        return "Not Supported";
    }

    /**
     * Méthode interne pour charger l'île d'un joueur via SkylliaAPI.
     */
    private static @NotNull Optional<Island> loadIslandByUUID(@NotNull UUID playerId) {
        CompletableFuture<Island> future = SkylliaAPI.getIslandByPlayerId(playerId);
        if (future == null) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(future.get(5, TimeUnit.SECONDS));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.error("Impossible de charger l'île pour le joueur {} : {}", playerId, e.getMessage());
            return Optional.empty();
        }
    }

    private static String processIsland(Optional<Island> islandOpt, UUID playerId, String placeholder) {
        if (islandOpt.isEmpty()) {
            return "";
        }
        Island island = islandOpt.get();

        // On gère les placeholders "island_xxx"
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
                island.getId(),
                roleType,
                permissions.getPermissionType()
        );
        PermissionManager permissionManager = new PermissionManager(permissionRoleIsland.permission());

        return String.valueOf(permissionManager.hasPermission(permissions));
    }

    private static String processGamerulePlaceholder(Island island, String placeholder) {
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
