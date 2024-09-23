package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
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
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class VisitSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(VisitSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("skyllia.island.command.visit")) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }
        if (args.length < 1) {
            LanguageToml.sendMessage(player, LanguageToml.messageVisitCommandNotEnoughArgs);
            return true;
        }

        try {
            String visitPlayer = args[0];
            UUID visitPlayerId;
            try {
                visitPlayerId = UUID.fromString(visitPlayer);
            } catch (IllegalArgumentException ignored) {
                visitPlayerId = Bukkit.getPlayerUniqueId(visitPlayer);
            }
            if (visitPlayerId == null) {
                LanguageToml.sendMessage(player, LanguageToml.messagePlayerNotFound);
                return true;
            }

            SkyblockManager skyblockManager = Main.getPlugin(Main.class).getInterneAPI().getSkyblockManager();
            Island island = skyblockManager.getIslandByPlayerId(visitPlayerId).join();
            if (island == null) {
                LanguageToml.sendMessage(player, LanguageToml.messageVisitPlayerHasNotIsland);
                return true;
            }

            if (!player.hasPermission("skyllia.island.command.visit.bypass")) {
                if (island.isPrivateIsland()) {
                    LanguageToml.sendMessage(player, LanguageToml.messageVisitIslandIsPrivate);
                    return true;
                }
                Players memberIsland = island.getMember(player.getUniqueId());
                if (memberIsland != null && memberIsland.getRoleType().equals(RoleType.BAN)) {
                    LanguageToml.sendMessage(player, LanguageToml.messageVisitIslandPlayerBanned);
                    return true;
                }
            }

            WarpIsland warpIsland = island.getWarpByName("home");
            player.getScheduler().execute(plugin, () -> {
                if (ConfigToml.changeGameModeWhenTeleportIsland) player.setGameMode(GameMode.SPECTATOR);
                Location loc;
                if (warpIsland == null) {
                    loc = RegionHelper.getCenterRegion(Bukkit.getWorld(WorldUtils.getWorldConfigs().get(0).name()), island.getPosition().x(), island.getPosition().z());
                } else {
                    loc = warpIsland.location();
                }
                loc.setY(loc.getY());
                player.teleportAsync(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                Main.getPlugin(Main.class).getInterneAPI().getPlayerNMS().setOwnWorldBorder(Main.getPlugin(Main.class), player, RegionHelper.getCenterRegion(loc.getWorld(), island.getPosition().x(), island.getPosition().z()), island.getSize(), 0, 0);
                if (ConfigToml.changeGameModeWhenTeleportIsland) player.setGameMode(GameMode.SURVIVAL);
                LanguageToml.sendMessage(player, LanguageToml.messageVisitIslandSuccess.replaceAll("%player%", visitPlayer));
            }, null, 0L);
        } catch (Exception exception) {
            logger.log(Level.FATAL, exception.getMessage(), exception);
            LanguageToml.sendMessage(player, LanguageToml.messageError);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
