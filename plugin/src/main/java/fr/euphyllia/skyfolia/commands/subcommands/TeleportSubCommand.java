package fr.euphyllia.skyfolia.commands.subcommands;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyfolia.commands.SubCommandInterface;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import fr.euphyllia.skyfolia.configuration.LanguageToml;
import fr.euphyllia.skyfolia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyfolia.utils.IslandUtils;
import fr.euphyllia.skyfolia.utils.RegionUtils;
import fr.euphyllia.skyfolia.utils.nms.v1_20_R2.PlayerNMS;
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

    public static void tpHomeIsland(Main plugin, Island island, Player player) {
        if (island == null) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerHasNotIsland);
            return;
        }

        WarpIsland warpIsland = island.getWarpByName("home");
        Location centerIsland = RegionUtils.getCenterRegion(Bukkit.getWorld(IslandUtils.getSchematic(null).worldName()), island.getPosition().regionX(), island.getPosition().regionZ());
        double rayon = island.getSize();
        player.getScheduler().run(plugin, scheduledTask1 -> {
            Location loc;
            if (warpIsland == null) {
                loc = centerIsland;
            } else {
                loc = warpIsland.location();
            }
            player.teleportAsync(loc);
            PlayerNMS.setOwnWorldBorder(plugin, player, centerIsland, "", rayon, 0, 0);
            player.setGameMode(GameMode.SURVIVAL);
        }, null);
    }

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        player.setGameMode(GameMode.SPECTATOR);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try {
            executor.execute(() -> {
                try {
                    SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
                    Island island = skyblockManager.getIslandByOwner(player.getUniqueId()).join();
                    tpHomeIsland(plugin, island, player);
                } catch (Exception exception) {
                    logger.log(Level.FATAL, exception.getMessage(), exception);
                    LanguageToml.sendMessage(plugin, player, "Impossible de se téléporter sur l'ile, vérifier les logs !");
                }
            });
        } finally {
            executor.shutdown();
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }

}
