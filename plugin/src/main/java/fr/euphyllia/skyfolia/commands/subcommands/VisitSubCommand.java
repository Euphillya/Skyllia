package fr.euphyllia.skyfolia.commands.subcommands;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.exceptions.UnsupportedMinecraftVersionException;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyfolia.commands.SubCommandInterface;
import fr.euphyllia.skyfolia.configuration.LanguageToml;
import fr.euphyllia.skyfolia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyfolia.utils.IslandUtils;
import fr.euphyllia.skyfolia.utils.PlayerUtils;
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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class VisitSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(VisitSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("skyfolia.island.command.visit")) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }
        if (args.length < 1) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messageVisitCommandNotEnoughArgs);
            return true;
        }

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try {
            executor.execute(() -> {
                try {
                    String visitPlayer = args[0];
                    UUID visitPlayerId = Bukkit.getPlayerUniqueId(visitPlayer);
                    if (visitPlayerId == null) {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerNotFound);
                        return;
                    }

                    SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
                    Island island = skyblockManager.getIslandByOwner(visitPlayerId).join();
                    if (island == null) {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messageVisitPlayerHasNotIsland);
                        return;
                    }

                    if (island.isPrivateIsland()) {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messageVisitIslandIsPrivate);
                        return;
                    }

                    WarpIsland warpIsland = island.getWarpByName("home");
                    player.getScheduler().run(plugin, scheduledTask1 -> {
                        player.setGameMode(GameMode.SPECTATOR);
                        Location loc;
                        if (warpIsland == null) {
                            loc = RegionUtils.getCenterRegion(Bukkit.getWorld(IslandUtils.getWorldConfigs().get(0).name()), island.getPosition().regionX(), island.getPosition().regionZ());
                        } else {
                            loc = warpIsland.location();
                        }
                        player.teleportAsync(loc);
                        try {
                            PlayerUtils.setOwnWorldBorder(plugin, player, RegionUtils.getCenterRegion(loc.getWorld(), island.getPosition().regionX(), island.getPosition().regionZ()), "", island.getSize(), 0, 0);
                        } catch (UnsupportedMinecraftVersionException e) {
                            logger.log(Level.FATAL, e.getMessage(), e);
                        }
                        player.setGameMode(GameMode.SURVIVAL);
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messageHomeIslandSuccess);
                    }, null);
                } catch (Exception exception) {
                    logger.log(Level.FATAL, exception.getMessage(), exception);
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messageError);
                }
            });
        } finally {
            executor.shutdown();
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
