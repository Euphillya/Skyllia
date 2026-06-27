package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.event.SkyblockCreateEvent;
import fr.euphyllia.skyllia.api.event.SkyblockLoadEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.*;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.cache.commands.CommandCacheExecution;
import fr.euphyllia.skyllia.cache.island.IslandCreationQueue;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.utils.IslandUtils;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class CreateSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(CreateSubCommand.class);

    public CompletableFuture<Void> runCreateIsland(Skyllia plugin, Player player, String[] args) {
        final UUID playerId = player.getUniqueId();
        final AtomicBoolean acquired = new AtomicBoolean(false);

        return CompletableFuture.supplyAsync(() -> {
            if (!CommandCacheExecution.tryAcquire(playerId, "create")) {
                ConfigLoader.language.sendMessage(player, "island.generic.command-in-progress");
                return null;
            }
            acquired.set(true);

            if (!PlayerUtils.hasPermission(player, "skyllia.island.command.create")) {
                ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
                return null;
            }

            Island existingIsland = SkylliaAPI.getIslandByPlayerId(playerId);
            if (existingIsland != null) {
                new HomeSubCommand().onExecute(plugin, player, args);
                return null;
            }

            List<String> schematicsKeys = ConfigLoader.schematicManager.getIslandTypes();
            if (schematicsKeys.isEmpty()) {
                ConfigLoader.language.sendMessage(player, "island.schematic-not-exist");
                return null;
            }

            String schemKey = resolveSchematicKey(args.length > 0 ? args[0] : null, schematicsKeys);
            Map<String, SchematicSetting> schematicSettingMap = IslandUtils.getSchematic(schemKey);
            if (schematicSettingMap == null || schematicSettingMap.isEmpty()) {
                ConfigLoader.language.sendMessage(player, "island.schematic-not-exist");
                return null;
            }

            IslandSettings islandSettings = IslandUtils.getIslandSettings(schemKey);
            if (islandSettings == null) {
                ConfigLoader.language.sendMessage(player, "island.type-not-exist");
                return null;
            }

            if (!PlayerUtils.hasPermission(player, "skyllia.island.command.create.%s".formatted(schemKey))) {
                ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
                return null;
            }

            ConfigLoader.language.sendMessage(player, "island.create-in-progress");
            UUID idIsland = UUID.randomUUID();

            Players owners = new Players(player.getUniqueId(), player.getName(), null, RoleType.OWNER);
            boolean isCreate = SkylliaAPI.createIsland(idIsland, islandSettings, owners);
            if (!isCreate) {
                ConfigLoader.language.sendMessage(player, "island.generic-error");
                return null;
            }

            Island island = SkylliaAPI.getIslandByIslandId(idIsland);
            if (island == null) {
                ConfigLoader.language.sendMessage(player, "island.generic-error");
                return null;
            }

            new SkyblockCreateEvent(island, playerId).callEvent();

            return new IslandCreationContext(island, schematicSettingMap);
        }).thenCompose(context -> {
            if (context == null) {
                return CompletableFuture.completedFuture(null);
            }

            return pasteAllSchematics(plugin, player, context.island(), context.schematicSettingMap())
                    .handle((result, throwable) -> {
                        if (throwable != null) {
                            logger.error("Island creation failed for {}: {}", context.island().getId(), throwable.getMessage(), throwable);
                            ConfigLoader.language.sendMessage(player, "island.generic-error");
                        } else {
                            ConfigLoader.language.sendMessage(player, "island.create-finish");
                        }
                        return (Void) null;
                    });
        }).exceptionally(throwable -> {
            logger.error("Island creation failed for {}: {}", playerId, throwable.getMessage(), throwable);
            ConfigLoader.language.sendMessage(player, "island.generic-error");
            return null;
        }).whenComplete((result, throwable) -> {
            if (acquired.get()) {
                CommandCacheExecution.removeCommandExec(playerId, "create");
            }
        });
    }

    private String resolveSchematicKey(String requestedKey, List<String> schematicsKeys) {
        if (requestedKey != null && schematicsKeys.contains(requestedKey)) {
            return requestedKey;
        }

        String defaultSchemKey = ConfigLoader.islandManager.getDefaultIslandKey();
        if (defaultSchemKey != null && schematicsKeys.contains(defaultSchemKey)) {
            return defaultSchemKey;
        }

        return schematicsKeys.getFirst();
    }

    private CompletableFuture<Void> pasteAllSchematics(Skyllia plugin, Player player, Island island,
                                                       Map<String, SchematicSetting> schematicMap) {
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        AtomicBoolean isFirst = new AtomicBoolean(true);
        for (Map.Entry<String, SchematicSetting> entry : schematicMap.entrySet()) {
            String worldName = entry.getKey();
            SchematicSetting setting = entry.getValue();
            boolean first = isFirst.getAndSet(false);

            chain = chain.thenCompose(ignored -> {
                Location center = RegionHelper.getCenterRegion(
                        Bukkit.getWorld(worldName),
                        island.getRegionCoordinate().x(),
                        island.getRegionCoordinate().z()
                );
                center.setY(setting.height());
                island.setCenterLocation(center);

                return Skyllia.getInstance().getInterneAPI()
                        .getSchematicHook(SchematicPlugin.fromString(setting.plugin()))
                        .paste(center, setting)
                        .thenAcceptAsync(success -> {
                            if (!success) {
                                island.setDisable(true);
                                throw new RuntimeException("Schematic paste failed for world " + worldName);
                            }
                            if (setting.minBuildHeight() != null) {
                                island.setBuildHeight(worldName, HeightType.MIN, setting.minBuildHeight());
                            }
                            if (setting.maxBuildHeight() != null) {
                                island.setBuildHeight(worldName, HeightType.MAX, setting.maxBuildHeight());
                            }
                            if (first) {
                                // After schematic paste, find the actual ground location and setup island
                                findGroundLocationAsync(center).thenComposeAsync(spawnLocation -> {
                                    island.addWarps("home", spawnLocation, true);
                                    island.setSpawnLocation(spawnLocation);

                                    Skyllia.getInstance().getInterneAPI()
                                            .getSkyblockManager()
                                            .cacheIslandAndIndex(island);

                                    new SkyblockLoadEvent(island).callEvent();
                                    
                                    return teleportAndApplyBorder(player, island, spawnLocation);
                                });
                            }
                        });
            });
        }
        return chain;
    }

    private CompletableFuture<Void> teleportAndApplyBorder(Player player, Island island, Location center) {
        Location spawnLoc = center.clone().add(0, 0.5, 0);
        return player.teleportAsync(spawnLoc, PlayerTeleportEvent.TeleportCause.PLUGIN)
                .thenAccept(successTeleport -> {
                    if (!successTeleport) {
                        return;
                    }
                    player.setVelocity(new Vector(0, 0, 0));
                    player.setFallDistance(0);
                    if (PlayerUtils.hasPermission(player, "skyllia.island.worldborder.bypass")) {
                        return;
                    }
                    WorldBorder border = player.getWorldBorder();
                    if (border == null) border = Bukkit.createWorldBorder();
                    border.setCenter(center);
                    border.setSize(island.getSize());
                    player.setWorldBorder(border);
                });
    }

    @Override
    public void onExecute(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return;
        }

        if (IslandCreationQueue.isQueued(player.getUniqueId())) {
            ConfigLoader.language.sendMessage(player, "island.create.already-in-queue");
            return;
        }

        if (!PlayerUtils.hasPermission(player, "skyllia.island.command.create")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return;
        }

        boolean bypass = ConfigLoader.general.getIslandSettings().allowBypassQueue()
                && PlayerUtils.hasPermission(player, "skyllia.island.bypass.queue");

        if (bypass) {
            runCreateIsland(Skyllia.getInstance(), player, args);
        } else {
            IslandCreationQueue.queuePlayer(player, args);
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();
            List<String> nameSchem = ConfigLoader.schematicManager.getIslandTypes();
            if (nameSchem.isEmpty()) {
                return Collections.emptyList();
            }

            List<String> list = new ArrayList<>();
            for (String schem : nameSchem) {
                if (PlayerUtils.hasPermission(sender, "skyllia.island.command.create.%s".formatted(schem))) {
                    if (schem.toLowerCase().startsWith(partial)) {
                        list.add(schem);
                    }
                }
            }
            return list;
        }

        return Collections.emptyList();
    }

    private record IslandCreationContext(Island island, Map<String, SchematicSetting> schematicSettingMap) {
    }

    /**
     * Finds the highest solid ground block at the given XZ coordinates asynchronously.
     * Uses getChunkAtAsync for better performance by ensuring chunk is loaded before access.
     * If no solid block is found at center, expands search radius in a spiral pattern.
     *
     * @param center Location with the center XZ coordinates and schematic Y height
     * @return CompletableFuture with the location of the highest solid block
     */
    private CompletableFuture<Location> findGroundLocationAsync(Location center) {
        org.bukkit.World world = center.getWorld();
        if (world == null) {
            return CompletableFuture.completedFuture(center);
        }

        int centerX = center.getBlockX();
        int centerZ = center.getBlockZ();
        int startY = center.getBlockY();
        
        // Get world height limits
        int maxY = Math.min(startY + 20, world.getMaxHeight() - 1);
        int minY = Math.max(startY - 10, world.getMinHeight());

        // Load chunk asynchronously first, then search for ground
        return world.getChunkAtAsync(centerX >> 4, centerZ >> 4).thenApply(chunk -> {
            try {
                // First try center location
                Location result = searchColumnForGround(world, centerX, centerZ, minY, maxY);
                if (result != null) {
                    return result;
                }
                
                // If center has no solid block, expand search in spiral pattern
                int maxRadius = 10; // Maximum search radius
                for (int radius = 1; radius <= maxRadius; radius++) {
                    // Search in a square spiral pattern
                    for (int dx = -radius; dx <= radius; dx++) {
                        for (int dz = -radius; dz <= radius; dz++) {
                            // Only check the outer ring of current radius
                            if (Math.abs(dx) != radius && Math.abs(dz) != radius) {
                                continue;
                            }
                            
                            int x = centerX + dx;
                            int z = centerZ + dz;
                            
                            result = searchColumnForGround(world, x, z, minY, maxY);
                            if (result != null) {
                                return result;
                            }
                        }
                    }
                }
                
                // If no solid block found within radius, return the original center location
                return center;
            } catch (Exception e) {
                logger.error("Error finding ground location", e);
                return center;
            }
        });
    }
    
    /**
     * Searches a single column (X,Z) for the highest solid block.
     *
     * @param world The world
     * @param x Block X coordinate
     * @param z Block Z coordinate
     * @param minY Minimum Y to search
     * @param maxY Maximum Y to search
     * @return Location of solid block + 1, or null if none found
     */
    private Location searchColumnForGround(org.bukkit.World world, int x, int z, int minY, int maxY) {
        for (int y = maxY; y >= minY; y--) {
            org.bukkit.block.Block block = world.getBlockAt(x, y, z);
            
            // Check if the block is solid (not air, not water, not lava, etc.)
            if (block.getType().isSolid() && !block.isPassable()) {
                return new Location(world, x + 0.5, y + 1.0, z + 0.5);
            }
        }
        return null;
    }
}