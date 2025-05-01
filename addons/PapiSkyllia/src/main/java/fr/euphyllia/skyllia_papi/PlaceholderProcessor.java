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
import fr.euphyllia.skyllia.cache.rules.PermissionGameRuleInIslandCache;
import fr.euphyllia.skyllia.cache.rules.PermissionRoleInIslandCache;
import fr.euphyllia.skyllia_papi.hook.BankPlaceHolder;
import fr.euphyllia.skyllia_papi.hook.OrePlaceHolder;
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

/**
 * Main class for processing placeholders in the Skyllia plugin.
 * Manages the caching of player islands and processes various types of placeholders.
 */
public class PlaceholderProcessor {

    /**
     * Logger used for logging messages.
     */
    private static final Logger LOGGER = LogManager.getLogger(PlaceholderProcessor.class);

    /**
     * Formatter for decimal numbers.
     */
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    /**
     * Indicates if the SkylliaOre addon is enabled.
     */
    private static final boolean SKYLLIA_ORE_ADDON;

    /**
     * Indicates if the SkylliaValue addon is enabled.
     * The plugin is owned by Excalia,
     * if you want it on your server you have to ask the admins on their <a href="https://discord.gg/excalia">Discord</a>
     */
    private static final boolean SKYLLIA_VALUE_ADDON;

    /**
     * Indicates if the SkylliaBank addon is enabled.
     */
    private static final boolean SKYLLIA_BANK_ADDON;
    /**
     * Cache that maps a player's UUID to their island.
     * <p>
     * - {@code expireAfterWrite(5, SECONDS)}: removes from the cache 5 seconds after writing.
     * - {@code refreshAfterWrite(3, SECONDS)}: attempts a background refresh after 3 seconds.
     * </p>
     */
    private static final LoadingCache<UUID, Optional<Island>> ISLAND_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .refreshAfterWrite(3, TimeUnit.SECONDS)
            .build(new CacheLoader<>() {
                @Override
                public @NotNull Optional<Island> load(@NotNull UUID playerId) {
                    return loadIslandByUUID(playerId);
                }

                /**
                 * Handles the background refresh of a player's island.
                 *
                 * @param playerId the player's UUID
                 * @param oldValue the old island value
                 * @param executor the executor for the asynchronous task
                 * @return a {@link CompletableFuture} containing the refreshed island
                 */
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
     * Cache that stores the final result of a placeholder (String) based on the playerId and the placeholder.
     * Uses the same expiration and refresh logic as {@link #ISLAND_CACHE}.
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

                /**
                 * Handles the background refresh of a placeholder.
                 *
                 * @param key      the cache key containing the player's UUID and the placeholder
                 * @param oldValue the old placeholder value
                 * @param executor the executor for the asynchronous task
                 * @return a {@link CompletableFuture} containing the new placeholder value
                 */
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

    static {
        SKYLLIA_ORE_ADDON = Bukkit.getPluginManager().getPlugin("SkylliaOre") != null;
        SKYLLIA_VALUE_ADDON = Bukkit.getPluginManager().getPlugin("SkylliaValue") != null;
        SKYLLIA_BANK_ADDON = Bukkit.getPluginManager().getPlugin("SkylliaBank") != null;
    }

    /**
     * Entry point for retrieving a placeholder value.
     *
     * @param playerId    the player's UUID
     * @param placeholder the placeholder to process (e.g., "island_size", "permissions_modify_command", etc.)
     * @return the placeholder value as a string
     */
    public static String process(UUID playerId, String placeholder) {
        // Quickly check if the player has an island
        Optional<Island> islandOpt = Optional.ofNullable(ISLAND_CACHE.get(playerId))
                .orElse(Optional.empty());
        if (islandOpt.isEmpty()) {
            return "";
        }

        if (placeholder.startsWith("island")) {
            return PLACEHOLDER_CACHE.get(new CacheKey(playerId, placeholder));
        } else if (placeholder.startsWith("permissions")) {
            return processPermissionsPlaceholder(islandOpt.get(), playerId, placeholder);
        } else if (placeholder.startsWith("gamerule")) {
            return processGamerulePlaceholder(islandOpt.get(), placeholder);
        } else if (placeholder.startsWith("ore") && SKYLLIA_ORE_ADDON) {
            return OrePlaceHolder.processPlaceholder(islandOpt.get(), playerId, placeholder);
        } else if (placeholder.startsWith("bank") && SKYLLIA_BANK_ADDON) {
            return BankPlaceHolder.processPlaceholder(islandOpt.get(), playerId, placeholder);
        }

        return "Not Supported";
    }

    /**
     * Internal method to load a player's island via the SkylliaAPI.
     *
     * @param playerId the player's UUID
     * @return an {@link Optional} containing the island if found, otherwise empty
     */
    private static @NotNull Optional<Island> loadIslandByUUID(@NotNull UUID playerId) {
        CompletableFuture<Island> future = SkylliaAPI.getIslandByPlayerId(playerId);
        if (future == null) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(future.get(5, TimeUnit.SECONDS));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.error("Unable to load island for player {}: {}", playerId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Processes placeholders related to the player's island.
     *
     * @param islandOpt   the player's island wrapped in an {@link Optional}
     * @param playerId    the player's UUID
     * @param placeholder the placeholder to process
     * @return the placeholder value as a string
     */
    private static String processIsland(Optional<Island> islandOpt, UUID playerId, String placeholder) {
        if (islandOpt.isEmpty()) {
            return "";
        }
        Island island = islandOpt.get();

        // Handle "island_xxx" placeholders
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

    /**
     * Processes placeholders related to permissions.
     *
     * @param island      the player's island
     * @param playerId    the player's UUID
     * @param placeholder the placeholder to process
     * @return the placeholder value as a string
     */
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

    /**
     * Processes placeholders related to game rules.
     *
     * @param island      the player's island
     * @param placeholder the placeholder to process
     * @return the placeholder value as a string
     */
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

        long permissionChecker = PermissionGameRuleInIslandCache.getGameRule(island.getId());
        PermissionManager permissionManager = new PermissionManager(permissionChecker);

        return String.valueOf(permissionManager.hasPermission(gameRuleIsland.getPermissionValue()));
    }

    /**
     * Record representing a cache key with playerId and placeholder.
     *
     * @param playerId    the player's UUID
     * @param placeholder the placeholder string
     */
    private record CacheKey(UUID playerId, String placeholder) {
    }
}
