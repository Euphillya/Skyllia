package fr.euphyllia.skyllia.listeners.skyblockevents;

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
import org.bukkit.WorldBorder;
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
            if (bPlayer == null || !bPlayer.isOnline() || !WorldUtils.isWorldSkyblock(bPlayer.getWorld().getName())) continue;

            Location centerIsland = RegionHelper.getCenterRegion(bPlayer.getWorld(), event.getIsland().getPosition().x(), event.getIsland().getPosition().z());
            setWorldBorder(bPlayer, centerIsland, event.getNewSize());
        }
    }

    private void setWorldBorder(Player player, Location center, double size) {
        WorldBorder border = player.getWorldBorder();
        if (border == null) {
            border = Bukkit.createWorldBorder();
        }
        border.setCenter(center);
        border.setSize(size);
        player.setWorldBorder(border);
    }
}