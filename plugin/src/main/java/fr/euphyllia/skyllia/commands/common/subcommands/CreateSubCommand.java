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
        return CompletableFuture.runAsync(() -> {

            final UUID playerId = player.getUniqueId();

            if (!CommandCacheExecution.tryAcquire(playerId, "create")) {
                ConfigLoader.language.sendMessage(player, "island.generic.command-in-progress");
                return;
            }

            if (!PlayerUtils.hasPermission(player, "skyllia.island.command.create")) {
                ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
                CommandCacheExecution.removeCommandExec(playerId, "create");
                return;
            }

            Island existingIsland = SkylliaAPI.getIslandByPlayerId(playerId);
            if (existingIsland != null) {
                new HomeSubCommand().onExecute(plugin, player, args);
                CommandCacheExecution.removeCommandExec(playerId, "create");
                return;
            }

            List<String> schematicsKeys = ConfigLoader.schematicManager.getIslandTypes();
            if (schematicsKeys.isEmpty()) {
                ConfigLoader.language.sendMessage(player, "island.schematic-not-exist");
                CommandCacheExecution.removeCommandExec(playerId, "create");
                return;
            }

            String schemKey = (args.length > 0 && schematicsKeys.contains(args[0])) ? args[0] : schematicsKeys.getFirst();
            Map<String, SchematicSetting> schematicSettingMap = IslandUtils.getSchematic(schemKey);
            if (schematicSettingMap == null || schematicSettingMap.isEmpty()) {
                ConfigLoader.language.sendMessage(player, "island.schematic-not-exist");
                CommandCacheExecution.removeCommandExec(playerId, "create");
                return;
            }

            IslandSettings islandSettings = IslandUtils.getIslandSettings(schemKey);
            if (islandSettings == null) {
                ConfigLoader.language.sendMessage(player, "island.type-not-exist");
                CommandCacheExecution.removeCommandExec(playerId, "create");
                return;
            }

            if (!PlayerUtils.hasPermission(player, "skyllia.island.command.create.%s".formatted(schemKey))) {
                ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
                CommandCacheExecution.removeCommandExec(playerId, "create");
                return;
            }

            ConfigLoader.language.sendMessage(player, "island.create-in-progress");
            UUID idIsland = UUID.randomUUID();

            Players owners = new Players(player.getUniqueId(), player.getName(), null, RoleType.OWNER);
            boolean isCreate = SkylliaAPI.createIsland(idIsland, islandSettings, owners);
            if (!isCreate) {
                ConfigLoader.language.sendMessage(player, "island.generic-error");
                CommandCacheExecution.removeCommandExec(playerId, "create");
                return;
            }

            Island island = SkylliaAPI.getIslandByIslandId(idIsland);
            if (island == null) {
                ConfigLoader.language.sendMessage(player, "island.generic-error");
                CommandCacheExecution.removeCommandExec(playerId, "create");
                return;
            }

            new SkyblockCreateEvent(island, playerId).callEvent();

            pasteAllSchematics(plugin, player, island, schematicSettingMap)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            logger.error("Island creation failed for {}: {}", island.getId(), throwable.getMessage());
                            ConfigLoader.language.sendMessage(player, "island.generic-error");
                        } else {
                            ConfigLoader.language.sendMessage(player, "island.create-finish");
                        }
                        CommandCacheExecution.removeCommandExec(playerId, "create");
                    });
        });
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
                        island.getPosition().x(),
                        island.getPosition().z()
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
                                island.addWarps("home", center, true);

                                Skyllia.getInstance().getInterneAPI()
                                        .getSkyblockManager()
                                        .cacheIslandAndIndex(island);

                                new SkyblockLoadEvent(island).callEvent();
                                Location spawnLoc = center.clone().add(0, 0.5, 0);
                                player.teleportAsync(spawnLoc, PlayerTeleportEvent.TeleportCause.PLUGIN)
                                        .thenRun(() -> {
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
                        });
            });
        }
        return chain;
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
}
