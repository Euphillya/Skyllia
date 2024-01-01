package fr.euphyllia.skyfolia.commands.subcommands;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.model.IslandType;
import fr.euphyllia.skyfolia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyfolia.commands.SubCommandInterface;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import fr.euphyllia.skyfolia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyfolia.utils.IslandUtils;
import fr.euphyllia.skyfolia.utils.PlayerUtils;
import fr.euphyllia.skyfolia.utils.RegionUtils;
import fr.euphyllia.skyfolia.utils.WorldEditUtils;
import fr.euphyllia.skyfolia.utils.exception.MaxIslandSizeExceedException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class CreateSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(CreateSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if (!(sender instanceof Player player)) {
                return true;
            }
            IslandType islandType = IslandUtils.getIslandType(args.length == 0 ? null : args[0]);
            if (islandType == null) {
                logger.info("manque arguments");
                return false;
            }
            player.setGameMode(GameMode.SPECTATOR);
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            try {
                executor.execute(() -> {
                    SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
                    Island island = skyblockManager.getIslandByOwner(player.getUniqueId()).join();

                    if (island == null) {
                        player.sendMessage("L'ile en création");
                        island = skyblockManager.createIsland(player.getUniqueId(), islandType).join();
                        if (island == null) {
                            logger.fatal("island not create in database");
                            return;
                        }
                        logger.info("inscrit");

                        Location center = RegionUtils.getCenterRegion(Bukkit.getWorld(islandType.worldName()), island.getPosition().regionX(), island.getPosition().regionZ());
                        this.pasteSchematic(plugin, island, center, islandType);
                        this.setFirstHome(island, center);
                        this.restoreGameMode(plugin, player, center);
                        PlayerUtils.setOwnWorldBorder(plugin, player, center, "osef", island.getSize(), 0,0);
                    } else {
                        WarpIsland home = island.getWarpByName("home");
                        player.sendMessage("Vous avez déjà une île");
                        int regionX = island.getPosition().regionX();
                        int regionZ = island.getPosition().regionZ();
                        Location center = RegionUtils.getCenterRegion(Bukkit.getWorld(islandType.worldName()), island.getPosition().regionX(), island.getPosition().regionZ());
                        int rayon = island.getSize();
                        player.getScheduler().run(plugin, scheduledTask -> {
                            if (home == null) {
                                player.teleportAsync(RegionUtils.getCenterRegion(Bukkit.getWorld(islandType.worldName()), regionX, regionZ));
                            } else {
                                player.teleportAsync(home.location());
                            }
                            PlayerUtils.setOwnWorldBorder(plugin, player, center, "osef", rayon, 0,0);
                        }, null);
                    }
                });
            } finally {
                executor.shutdown();
            }
            return true;
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage());
            sender.sendMessage("Impossible de créer l'ile, vérifier les logs !");
            return false;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return ConfigToml.islandTypes.keySet().stream().toList();
        } else {
            return new ArrayList<>();
        }
    }

    private void pasteSchematic(Main plugin, Island island, Location center, IslandType islandType) {
        switch (WorldEditUtils.worldEditVersion()) {
            case WORLD_EDIT -> Bukkit.getServer().getRegionScheduler().run(plugin, center, t -> {
                WorldEditUtils.pasteSchematicWE(plugin.getInterneAPI(), center, islandType);
            });
            case FAST_ASYNC_WORLD_EDIT -> WorldEditUtils.pasteSchematicWE(plugin.getInterneAPI(), center, islandType);
            case UNDEFINED -> {
                island.setDisable(true); // Désactiver l'ile !
                throw new RuntimeException("Unsupported Plugin Paste");
            }
        }
    }

    private void restoreGameMode(Main plugin, Player player, Location center) {
        player.getScheduler().run(plugin, t -> {
            player.teleportAsync(center);
            player.setGameMode(GameMode.SURVIVAL);
        }, null);
    }

    private void setFirstHome(Island island, Location center) {
        island.addWarps("home", center);
    }
}
