package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.commands.SubCommandInterface;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyllia.utils.RegionUtils;
import fr.euphyllia.skyllia.utils.WorldUtils;
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
        if (!player.hasPermission("skyllia.island.command.visit")) {
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
                    UUID visitPlayerId;
                    try {
                        visitPlayerId = UUID.fromString(visitPlayer);
                    } catch (IllegalArgumentException ignored) {
                        visitPlayerId = Bukkit.getPlayerUniqueId(visitPlayer);
                    }
                    if (visitPlayerId == null) {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerNotFound);
                        return;
                    }

                    SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
                    Island island = skyblockManager.getIslandByPlayerId(visitPlayerId).join();
                    if (island == null) {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messageVisitPlayerHasNotIsland);
                        return;
                    }

                    if (!player.hasPermission("skyllia.island.command.visit.bypass")) {
                        if (island.isPrivateIsland()) {
                            LanguageToml.sendMessage(plugin, player, LanguageToml.messageVisitIslandIsPrivate);
                            return;
                        }
                        Players memberIsland = island.getMember(player.getUniqueId());
                        if (memberIsland != null && memberIsland.getRoleType().equals(RoleType.BAN)) {
                            LanguageToml.sendMessage(plugin, player, LanguageToml.messageVisitIslandPlayerBanned);
                            return;
                        }
                    }

                    WarpIsland warpIsland = island.getWarpByName("home");
                    player.getScheduler().run(plugin, scheduledTask1 -> {
                        player.setGameMode(GameMode.SPECTATOR);
                        Location loc;
                        if (warpIsland == null) {
                            loc = RegionUtils.getCenterRegion(Bukkit.getWorld(WorldUtils.getWorldConfigs().get(0).name()), island.getPosition().x(), island.getPosition().z());
                        } else {
                            loc = warpIsland.location();
                        }
                        player.teleportAsync(loc);
                        plugin.getInterneAPI().getPlayerNMS().setOwnWorldBorder(plugin, player, RegionUtils.getCenterRegion(loc.getWorld(), island.getPosition().x(), island.getPosition().z()), island.getSize(), 0, 0);
                        player.setGameMode(GameMode.SURVIVAL);
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messageVisitIslandSuccess.replaceAll("%player%", visitPlayer));
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
