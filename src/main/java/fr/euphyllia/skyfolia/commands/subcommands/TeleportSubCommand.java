package fr.euphyllia.skyfolia.commands.subcommands;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.commands.SubCommandInterface;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import fr.euphyllia.skyfolia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyfolia.utils.RegionUtils;
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

public class TeleportSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(TeleportSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if (!(sender instanceof Player player)) {
                return true;
            }
            player.setGameMode(GameMode.SPECTATOR);
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            try {
                executor.execute(() -> {
                    SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
                    Island island = skyblockManager.getIslandByOwner(player).join();

                    if (island != null) {
                        Location[] home = {skyblockManager.getLocationWarp(island, "home").join()};
                        player.getScheduler().run(plugin, scheduledTask1 -> {
                            if (home[0] == null) {
                                home[0] = RegionUtils.getCenterRegion(Bukkit.getWorld(ConfigToml.islandTypes.get(island.getIslandType()).worldName()), island.getPosition().regionX(), island.getPosition().regionZ());
                            }
                            player.teleportAsync(home[0]);
                            player.setGameMode(GameMode.SURVIVAL);
                        }, null);
                    } else {
                        // Besoin de creer une ile
                        player.sendMessage("Faut créer une ile");
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
        return new ArrayList<>();
    }

}
