package fr.euphyllia.skyllia.listeners.bukkitevents.player;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.configuration.PortalConfig;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.cache.CacheIsland;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(TeleportEvent.class);

    public TeleportEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler
    public void onPlayerHasAccessIsland(final PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        if (event.getPlayer().hasPermission("skyllia.island.command.visit.bypass")) return;
        Location to = event.getTo();
        Runnable task = () -> {
            World world = to.getWorld();
            if (world == null || !WorldUtils.isWorldSkyblock(world.getName())) return;

            Island island = SkylliaAPI.getIslandByChunk(to.getChunk());
            if (island == null) {
                event.setCancelled(true);
                LanguageToml.sendMessage(event.getPlayer(), LanguageToml.messageVisitIslandIsPrivate);
                return;
            }
            if (CacheIsland.getIslandClosed(island.getId())) {
                event.setCancelled(true);
                LanguageToml.sendMessage(event.getPlayer(), LanguageToml.messageVisitIslandIsPrivate);
            }
        };
        Bukkit.getRegionScheduler().execute(api.getPlugin(), to, task);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleportEvent(final PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        Location to = event.getTo();
        Runnable task = () -> {
            World world = to.getWorld();
            if (world == null || !WorldUtils.isWorldSkyblock(world.getName())) return;

            Island island = SkylliaAPI.getIslandByChunk(to.getChunk());
            if (island == null) return;

            Location centerIsland = RegionHelper.getCenterRegion(world, island.getPosition().x(), island.getPosition().z());
            api.getPlayerNMS().setOwnWorldBorder(api.getPlugin(), event.getPlayer(), centerIsland, island.getSize(), 0, 0);
        };
        Bukkit.getRegionScheduler().execute(api.getPlugin(), to, task);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerUsePortal(final PlayerPortalEvent event) {
        if (event.isCancelled()) return;

        event.setCanCreatePortal(false);
        PlayerTeleportEvent.TeleportCause teleportCause = event.getCause();
        if (teleportCause == PlayerTeleportEvent.TeleportCause.END_PORTAL ||
                teleportCause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {

            Player player = event.getPlayer();
            Location location = player.getLocation();
            World world = location.getWorld();
            if (world == null) return;

            WorldConfig worldConfig = WorldUtils.getWorldConfig(world.getName());
            if (worldConfig == null) return;

            PortalConfig portalConfig = (teleportCause == PlayerTeleportEvent.TeleportCause.END_PORTAL)
                    ? worldConfig.endPortal()
                    : worldConfig.netherPortal();

            if (!portalConfig.enabled()) {
                event.setCancelled(true);
                return;
            }

            PermissionsIsland permission = (teleportCause == PlayerTeleportEvent.TeleportCause.END_PORTAL)
                    ? PermissionsIsland.USE_END_PORTAL
                    : PermissionsIsland.USE_NETHER_PORTAL;

            ListenersUtils.checkPermission(location, player, permission, event);
        }
    }
}
