package fr.euphyllia.skyllia.listeners.skyblockevents;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.configuration.PortalConfig;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.event.*;
import fr.euphyllia.skyllia.api.event.players.PlayerPrepareChangeWorldSkyblockEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class SkyblockEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(SkyblockEvent.class);

    public SkyblockEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSkyblockCreate(final SkyblockCreateEvent event) {
        this.api.getCacheManager().updateCacheIsland(event.getIsland(), event.getOwnerId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSkyblockChangePermission(final SkyblockChangePermissionEvent event) {
        this.api.getCacheManager().updatePermissionCacheIsland(event.getIsland());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSkyblockDelete(final SkyblockDeleteEvent event) {
        if (event.isCancelled()) return;
        this.api.getCacheManager().deleteCacheIsland(event.getIsland());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPrepareChangeWorldSkyblock(final PlayerPrepareChangeWorldSkyblockEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        if (event.getPortalType() == PlayerPrepareChangeWorldSkyblockEvent.PortalType.NETHER) {
            teleportOtherWorld(player, event, PermissionsIsland.USE_NETHER_PORTAL);
        } else if (event.getPortalType() == PlayerPrepareChangeWorldSkyblockEvent.PortalType.END) {
            teleportOtherWorld(player, event, PermissionsIsland.USE_END_PORTAL);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSkyblockLoad(final SkyblockLoadEvent event) {
        Players players = this.api.getSkyblockManager().getOwnerByIslandID(event.getIsland()).join();
        if (players == null) return;
        this.api.getCacheManager().updateCacheIsland(event.getIsland(), players.getMojangId());
    }

    @EventHandler
    public void onSkyblockSize(final SkyblockChangeSizeEvent event) {
        List<Players> players = event.getIsland().getMembers();
        for (Players player : players) {
            Player bPlayer = Bukkit.getPlayer(player.getMojangId());
            if (bPlayer != null && bPlayer.isOnline() && (Boolean.TRUE.equals(WorldUtils.isWorldSkyblock(bPlayer.getWorld().getName())))) {
                Location centerIsland = RegionHelper.getCenterRegion(bPlayer.getWorld(), event.getIsland().getPosition().x(), event.getIsland().getPosition().z());
                this.api.getPlayerNMS().setOwnWorldBorder(this.api.getPlugin(), bPlayer, centerIsland, event.getSizeIsland(), 0, 0);
            }
        }
    }

    private void teleportOtherWorld(Player player, PlayerPrepareChangeWorldSkyblockEvent event, PermissionsIsland permissionsIsland) {
        Island island = ListenersUtils.checkPermission(player.getLocation(), player, permissionsIsland, event);
        if (island == null) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
            return;
        }
        try {
            WorldConfig worldConfig = event.getWorldConfig();
            PortalConfig portalConfig;
            if (permissionsIsland.equals(PermissionsIsland.USE_NETHER_PORTAL)) {
                portalConfig = worldConfig.netherPortal();
            } else if (permissionsIsland.equals(PermissionsIsland.USE_END_PORTAL)) {
                portalConfig = worldConfig.endPortal();
            } else {
                LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
                return;
            }
            if (Boolean.FALSE.equals(WorldUtils.isWorldSkyblock(portalConfig.direction()))) {
                logger.log(Level.ERROR, "The %s world is not a skyblock world!".formatted(portalConfig.direction()));
                return;
            }
            World world = Bukkit.getWorld(portalConfig.direction());
            if (world == null) {
                logger.log(Level.ERROR, "The %s world is not loaded or not exist!".formatted(portalConfig.direction()));
                return;
            }
            Location playerLocation = player.getLocation();
            Location futurLocation = new Location(world, playerLocation.getBlockX(), playerLocation.getBlockY(), playerLocation.getBlockZ());
            Bukkit.getRegionScheduler().execute(SkylliaAPI.getPlugin(), futurLocation, () -> {
                int y = world.getMinHeight();
                while (!WorldUtils.isSafeLocation(futurLocation)) {
                    if (futurLocation.getBlockY() >= world.getMaxHeight()) return;
                    futurLocation.setY(y++);
                }
                PlayerChangeWorldSkyblockEvent worldSkyblockEvent = new PlayerChangeWorldSkyblockEvent(player, event.getPortalType(), futurLocation, true);
                Bukkit.getPluginManager().callEvent(worldSkyblockEvent);
                if (!worldSkyblockEvent.checkSafeLocation() || WorldUtils.isSafeLocation(worldSkyblockEvent.getTo())) {
                    player.teleportAsync(worldSkyblockEvent.getTo(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                    this.api.getPlayerNMS().setOwnWorldBorder(Main.getPlugin(Main.class), player,
                            RegionHelper.getCenterRegion(worldSkyblockEvent.getTo().getWorld(), island.getPosition().x(), island.getPosition().z()),
                            island.getSize(), 0, 0);
                } else {
                    LanguageToml.sendMessage(player, LanguageToml.messageLocationNotSafe);
                }
            });
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    @EventHandler
    public void onSkyblockChangeGameRule(final SkyblockChangeGameRuleEvent event) {
        this.api.getCacheManager().updateGameRuleCacheIsland(event.getIsland());
    }

}
