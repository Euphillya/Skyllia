package fr.euphyllia.skyllia.listeners.skyblockevents;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.event.SkyblockChangeSizeEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class SkyblockEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(SkyblockEvent.class);

    public SkyblockEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler
    public void onSkyblockSize(final SkyblockChangeSizeEvent event) {
        List<Players> players = event.getIsland().getMembers();
        for (Players player : players) {
            Player bPlayer = Bukkit.getPlayer(player.getMojangId());
            if (bPlayer != null && bPlayer.isOnline() && (WorldUtils.isWorldSkyblock(bPlayer.getWorld().getName()))) {
                Location centerIsland = RegionHelper.getCenterRegion(bPlayer.getWorld(), event.getIsland().getPosition().x(), event.getIsland().getPosition().z());
                this.api.getPlayerNMS().setOwnWorldBorder(this.api.getPlugin(), bPlayer, centerIsland, event.getNewSize(), 0, 0);
            }
        }
    }


    private Location findSafeLocation(World world, double x, double baseY, double z) {
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight();
        int y = minY;

        Location location = new Location(world, x, baseY, z);
        while (!WorldUtils.isSafeLocation(location) && y < maxY) {
            location.setY(y++);
        }

        return location;
    }

    private void teleportIfSafe(Player player, Island island, Location to) {
        if (to != null && WorldUtils.isSafeLocation(to)) {
            player.teleportAsync(to, PlayerTeleportEvent.TeleportCause.PLUGIN).thenRun(() -> {
                this.api.getPlayerNMS().setOwnWorldBorder(
                        Skyllia.getInstance(), player,
                        RegionHelper.getCenterRegion(to.getWorld(), island.getPosition().x(), island.getPosition().z()),
                        island.getSize(), 0, 0
                );
            });
        } else {
            ConfigLoader.language.sendMessage(player, "island.generic.location-unsafe");
        }
    }

    private boolean sameChunk(Location loc1, Location loc2) {
        return loc1.getWorld().equals(loc2.getWorld())
                && (loc1.getBlockX() >> 4) == (loc2.getBlockX() >> 4)
                && (loc1.getBlockZ() >> 4) == (loc2.getBlockZ() >> 4);
    }

}
