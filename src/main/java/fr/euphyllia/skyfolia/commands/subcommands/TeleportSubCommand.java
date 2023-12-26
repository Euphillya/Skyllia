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

public class TeleportSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(this);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if (sender instanceof Player player) {
                player.setGameMode(GameMode.SPECTATOR);
                Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
                    SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
                    Island island = skyblockManager.getIslandByOwner(player).join();

                    if (island != null) {
                        Location center = RegionUtils.getCenterRegion(Bukkit.getWorld(ConfigToml.islandTypes.get(island.getIslandType()).worldName()), island.getPosition().regionX(), island.getPosition().regionZ());
                        player.getScheduler().run(plugin, scheduledTask1 -> {
                            player.teleportAsync(center);
                            player.setGameMode(GameMode.SURVIVAL);
                        }, null);
                    } else {
                        // Besoin de creer une ile
                        player.sendMessage("Faut créer une ile");
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
        return new ArrayList<>();
    }

}
