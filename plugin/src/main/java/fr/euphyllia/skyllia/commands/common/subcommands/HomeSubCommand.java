package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HomeSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(HomeSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }
        if (!PermissionImp.hasPermission(sender, "skyllia.island.command.home")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        try {
            SkyblockManager skyblockManager = Skyllia.getPlugin(Skyllia.class).getInterneAPI().getSkyblockManager();
            Island island = skyblockManager.getIslandByPlayerId(player.getUniqueId()).join();
            if (island == null) {
                ConfigLoader.language.sendMessage(player, "island.player.no-island");
                return true;
            }

            WarpIsland warpIsland = island.getWarpByName("home");
            double rayon = island.getSize();

            player.getScheduler().execute(plugin, () -> {
                Location loc;
                if (warpIsland == null) {
                    loc = RegionHelper.getCenterRegion(Bukkit.getWorld(WorldUtils.getWorldConfigs().getFirst().getWorldName()), island.getPosition().x(), island.getPosition().z());
                } else {
                    loc = warpIsland.location();
                }
                loc.add(0, 0.5, 0);
                player.teleportAsync(loc, PlayerTeleportEvent.TeleportCause.PLUGIN).thenRun(() -> {
                    player.setVelocity(new Vector(0, 0, 0));
                    player.setFallDistance(0);
                });
                Skyllia.getPlugin(Skyllia.class).getInterneAPI().getPlayerNMS().setOwnWorldBorder(Skyllia.getPlugin(Skyllia.class), player, RegionHelper.getCenterRegion(loc.getWorld(), island.getPosition().x(), island.getPosition().z()), rayon, 0, 0);
                ConfigLoader.language.sendMessage(player, "island.home.success");
            }, null, 1L);
        } catch (Exception exception) {
            logger.log(Level.FATAL, exception.getMessage(), exception);
            ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
        }
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return new ArrayList<>();
    }

}
