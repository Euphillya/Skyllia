package fr.euphyllia.skyllia.commands.admin.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.event.SkyblockCreateEvent;
import fr.euphyllia.skyllia.api.event.SkyblockLoadEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.*;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.utils.IslandUtils;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ForceCreateSubCommands implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(ForceCreateSubCommands.class);

    @Override
    public void onExecute(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!PlayerUtils.hasPermission(sender, "skyllia.admins.commands.island.create")) {
            ConfigLoader.language.sendMessage(sender, "island.player.permission-denied");
            return;
        }

        // /isadmin create <player|uuid> [schem]
        if (args.length < 1) {
            ConfigLoader.language.sendMessage(sender, "island.admin.create-args-missing");
            return;
        }

        String playerArg = args[0];
        String schemArg = args.length >= 2 ? args[1] : null;

        try {
            // Résolution player/UUID
            UUID targetId;
            String targetName;
            Player onlineTarget = Bukkit.getPlayer(playerArg);
            if (onlineTarget != null) {
                targetId = onlineTarget.getUniqueId();
                targetName = onlineTarget.getName();
            } else {
                try {
                    targetId = UUID.fromString(playerArg);
                    targetName = Bukkit.getOfflinePlayer(targetId).getName();
                    if (targetName == null) targetName = targetId.toString();
                } catch (IllegalArgumentException ignored) {
                    UUID resolvedId = Bukkit.getPlayerUniqueId(playerArg);
                    if (resolvedId == null) {
                        ConfigLoader.language.sendMessage(sender, "island.admin.player-not-found");
                        return;
                    }
                    targetId = resolvedId;
                    targetName = playerArg;
                }
            }

            // Vérification : le joueur a déjà une île
            Island existingIsland = SkylliaAPI.getIslandByPlayerId(targetId);
            if (existingIsland != null) {
                ConfigLoader.language.sendMessage(sender, "island.admin.create-already-has-island");
                return;
            }

            // Résolution du schématic
            List<String> schematicsKeys = ConfigLoader.schematicManager.getIslandTypes();
            if (schematicsKeys.isEmpty()) {
                ConfigLoader.language.sendMessage(sender, "island.schematic-not-exist");
                return;
            }

            String schemKey = resolveSchematicKey(schemArg, schematicsKeys);

            Map<String, SchematicSetting> schematicSettingMap = IslandUtils.getSchematic(schemKey);
            if (schematicSettingMap == null || schematicSettingMap.isEmpty()) {
                ConfigLoader.language.sendMessage(sender, "island.schematic-not-exist");
                return;
            }

            IslandSettings islandSettings = IslandUtils.getIslandSettings(schemKey);
            if (islandSettings == null) {
                ConfigLoader.language.sendMessage(sender, "island.type-not-exist");
                return;
            }

            ConfigLoader.language.sendMessage(sender, "island.create-in-progress");

            final UUID finalTargetId = targetId;
            final String finalTargetName = targetName;

            UUID idIsland = UUID.randomUUID();
            Players owner = new Players(finalTargetId, finalTargetName, null, RoleType.OWNER);

            boolean isCreate = SkylliaAPI.createIsland(idIsland, islandSettings, owner);
            if (!isCreate) {
                ConfigLoader.language.sendMessage(sender, "island.generic-error");
                return;
            }

            Island island = SkylliaAPI.getIslandByIslandId(idIsland);
            if (island == null) {
                ConfigLoader.language.sendMessage(sender, "island.generic-error");
                return;
            }

            new SkyblockCreateEvent(island, finalTargetId).callEvent();

            pasteAllSchematics(island, schematicSettingMap, finalTargetId)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            logger.error("Admin island creation failed for {}: {}", island.getId(), throwable.getMessage());
                            ConfigLoader.language.sendMessage(sender, "island.generic-error");
                        } else {
                            ConfigLoader.language.sendMessage(sender, "island.create-finish");
                        }
                    });

        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            ConfigLoader.language.sendMessage(sender, "island.generic.unexpected-error");
        }
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

    private CompletableFuture<Void> pasteAllSchematics(Island island,
                                                       Map<String, SchematicSetting> schematicMap,
                                                       UUID ownerId) {
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
                                // After schematic paste, find the actual ground location
                                findGroundLocationAsync(center).thenAcceptAsync(spawnLocation -> {
                                    island.addWarps("home", spawnLocation, true);
                                    island.setSpawnLocation(spawnLocation);

                                    Skyllia.getInstance().getInterneAPI()
                                            .getSkyblockManager()
                                            .cacheIslandAndIndex(island);

                                    new SkyblockLoadEvent(island).callEvent();

                                    // Téléportation si le joueur est en ligne
                                    Player onlineOwner = Bukkit.getPlayer(ownerId);
                                    if (onlineOwner != null) {
                                        Location spawnLoc = spawnLocation.clone().add(0, 0.5, 0);
                                        onlineOwner.teleportAsync(spawnLoc,
                                                org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
                                    }
                                });
                            }
                        });
            });
        }
        return chain;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!PlayerUtils.hasPermission(sender, "skyllia.admins.commands.island.create")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .sorted()
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            String partial = args[1].trim().toLowerCase();
            return ConfigLoader.schematicManager.getIslandTypes().stream()
                    .filter(s -> s.toLowerCase().startsWith(partial))
                    .sorted()
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
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