package fr.euphyllia.skyllia.listeners.skyblockevents;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.event.*;
import fr.euphyllia.skyllia.api.event.players.PlayerChangeWorldSkyblockEvent;
import fr.euphyllia.skyllia.api.event.players.PlayerPrepareChangeWorldSkyblockEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
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
        this.api.getCacheManager().updateCacheIsland(event.getIsland());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSkyblockChangePermission(final SkyblockChangePermissionEvent event) {
        this.api.getCacheManager().updateCacheIsland(event.getIsland());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSkyblockDelete(final SkyblockDeleteEvent event) {
        this.api.getCacheManager().deleteCacheIsland(event.getIsland());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPrepareChangeWorldSkyblock(final PlayerPrepareChangeWorldSkyblockEvent event) {
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
        this.api.getCacheManager().updateCacheIsland(event.getIsland());
    }

    @EventHandler
    public void onSkyblockSize(final SkyblockChangeSizeEvent event) {
        List<Players> players = event.getIsland().getMembers();
        for (Players player : players) {
            Player bPlayer = Bukkit.getPlayer(player.getMojangId());
            if (bPlayer != null && bPlayer.isOnline() && (WorldUtils.isWorldSkyblock(bPlayer.getWorld().getName()))) {
                Location centerIsland = RegionHelper.getCenterRegion(bPlayer.getWorld(), event.getIsland().getPosition().x(), event.getIsland().getPosition().z());
                this.api.getPlayerNMS().setOwnWorldBorder(this.api.getPlugin(), bPlayer, centerIsland, event.getSizeIsland(), 0, 0);
            }
        }
    }

    private void teleportOtherWorld(Player player, PlayerPrepareChangeWorldSkyblockEvent event, PermissionsIsland permissionsIsland) {
        Island island = ListenersUtils.checkPermission(player.getLocation(), player, permissionsIsland, event);
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return;
        }

        WorldConfig worldConfig = event.getWorldConfig();
        String portalRedirectWorldName;

        if (permissionsIsland.equals(PermissionsIsland.USE_NETHER_PORTAL)) {
            portalRedirectWorldName = worldConfig.getPortalNether();
        } else if (permissionsIsland.equals(PermissionsIsland.USE_END_PORTAL)) {
            portalRedirectWorldName = worldConfig.getPortalEnd();
        } else {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return;
        }

        if (!WorldUtils.isWorldSkyblock(portalRedirectWorldName)) {
            logger.log(Level.ERROR, "The %s world is not a skyblock world!".formatted(portalRedirectWorldName));
            return;
        }

        World world = Bukkit.getWorld(portalRedirectWorldName);
        if (world == null) {
            logger.log(Level.ERROR, "The %s world is not loaded or does not exist!".formatted(portalRedirectWorldName));
            return;
        }

        Location playerLocation = player.getLocation();


        Bukkit.getRegionScheduler().execute(SkylliaAPI.getPlugin(), world, playerLocation.getBlockX() >> 4, playerLocation.getBlockZ() >> 4, () -> {
            Location initialLocation = findSafeLocation(world,
                    playerLocation.getBlockX(),
                    playerLocation.getBlockY(),
                    playerLocation.getBlockZ()
            );
            PlayerChangeWorldSkyblockEvent worldSkyblockEvent = new PlayerChangeWorldSkyblockEvent(
                    player, event.getPortalType(), initialLocation, true
            );
            Bukkit.getPluginManager().callEvent(worldSkyblockEvent);

            Location to = worldSkyblockEvent.getTo();

            // Retry with center island if the initial location is unsafe
            if (to == null || !WorldUtils.isSafeLocation(to)) {
                Location centerPaste = RegionHelper.getCenterRegion(world, island.getPosition().x(), island.getPosition().z());

                if (!sameChunk(initialLocation, centerPaste)) {
                    Bukkit.getRegionScheduler().execute(SkylliaAPI.getPlugin(), centerPaste, () -> {
                        Location fallbackLocation = findSafeLocation(
                                world,
                                centerPaste.getX(),
                                playerLocation.getBlockY(),
                                centerPaste.getZ()
                        );
                        teleportIfSafe(player, island, fallbackLocation);
                    });
                } else {
                    Location fallbackLocation = findSafeLocation(
                            world,
                            centerPaste.getX(),
                            playerLocation.getBlockY(),
                            centerPaste.getZ()
                    );
                    teleportIfSafe(player, island, fallbackLocation);
                }

            } else {
                teleportIfSafe(player, island, to);
            }
        });
    }

    @EventHandler
    public void onSkyblockChangeGameRule(final SkyblockChangeGameRuleEvent event) {
        this.api.getCacheManager().updateCacheIsland(event.getIsland());
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
            player.teleportAsync(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
            this.api.getPlayerNMS().setOwnWorldBorder(
                    Skyllia.getPlugin(Skyllia.class), player,
                    RegionHelper.getCenterRegion(to.getWorld(), island.getPosition().x(), island.getPosition().z()),
                    island.getSize(), 0, 0
            );
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
