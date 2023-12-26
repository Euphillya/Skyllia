package fr.euphyllia.skyfolia.commands.subcommands;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.model.IslandType;
import fr.euphyllia.skyfolia.commands.SubCommandInterface;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import fr.euphyllia.skyfolia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyfolia.utils.RegionUtils;
import fr.euphyllia.skyfolia.utils.WorldEditUtils;
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

public class CreateSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(this);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if (sender instanceof Player player) {
                IslandType islandType = this.getIslandType(args.length == 0 ? null : args[0]);
                if (islandType == null) {
                    logger.info("manque arguments");
                    return false;
                }
                player.setGameMode(GameMode.SPECTATOR);
                Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
                    SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
                    Island island = skyblockManager.getIslandByOwner(player).join();

                    if (island != null) {
                        Location center = RegionUtils.getCenterRegion(Bukkit.getWorld(islandType.worldName()), island.getPosition().regionX(), island.getPosition().regionZ());
                        player.sendMessage("Vous avez déjà une île");
                        player.teleportAsync(center);
                    } else {
                        player.sendMessage("L'ile en création");
                        island = skyblockManager.createIsland(player, islandType).join();
                        if (island == null) {
                            logger.fatal("island not create in database");
                            return;
                        }

                        Location center = RegionUtils.getCenterRegion(Bukkit.getWorld(islandType.worldName()), island.getPosition().regionX(), island.getPosition().regionZ());
                        switch (WorldEditUtils.worldEditVersion()) {
                            case WORLD_EDIT -> Bukkit.getServer().getRegionScheduler().run(plugin, center, t -> {
                                WorldEditUtils.pasteSchematicWE(plugin.getInterneAPI(), center, islandType);
                            });
                            case FAST_ASYNC_WORLD_EDIT -> WorldEditUtils.pasteSchematicWE(plugin.getInterneAPI(), center, islandType);
                            case UNDEFINED -> {
                                skyblockManager.disableIsland(island); // Désactiver l'ile !
                                throw new RuntimeException("Unsupported Plugin Paste");
                            }
                        }

                        player.getScheduler().run(plugin, scheduledTask1 -> {
                            player.teleportAsync(center);
                            player.setGameMode(GameMode.SURVIVAL);
                        }, null);
                    }
                });
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

    private @Nullable IslandType getIslandType(String name) {
        try {
            if (name == null) {
                return ConfigToml.islandTypes.values().stream().toList().get(0);
            } else {
                return ConfigToml.islandTypes.getOrDefault(name, null);
            }
        } catch (Exception e) {
            return null;
        }
    }
}
