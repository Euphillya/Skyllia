package fr.euphyllia.skyllia.listeners.bukkitevents.player;

import fr.euphyllia.energie.model.SchedulerType;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.configuration.PortalConfig;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.event.players.PlayerPrepareChangeWorldSkyblockEvent;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleportOnIsland(final PlayerTeleportEvent event) {
        if (event.isCancelled()) {
            return;
        }
        SkylliaAPI.getNativeScheduler()
                .runTask(SchedulerType.ASYNC, schedulerTask -> {
                    Location destination = event.getTo();
                    PlayerTeleportEvent.TeleportCause teleportCause = event.getCause();
                    if (teleportCause.equals(PlayerTeleportEvent.TeleportCause.END_PORTAL)) {
                        ListenersUtils.callPlayerPrepareChangeWorldSkyblockEvent(event.getPlayer(),
                                PlayerPrepareChangeWorldSkyblockEvent.PortalType.END, destination.getWorld().getName());
                    } else if (teleportCause.equals(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL)) {
                        ListenersUtils.callPlayerPrepareChangeWorldSkyblockEvent(event.getPlayer(),
                                PlayerPrepareChangeWorldSkyblockEvent.PortalType.NETHER, destination.getWorld().getName());
                    }
                });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerUsePortal(final PlayerPortalEvent event) {
        if (event.isCancelled()) return;
        event.setCanCreatePortal(false);
        PlayerTeleportEvent.TeleportCause teleportCause = event.getCause();
        if (teleportCause.equals(PlayerTeleportEvent.TeleportCause.END_PORTAL)) {
            Location location = event.getPlayer().getLocation();
            WorldConfig worldConfig = WorldUtils.getWorldConfig(location.getWorld().getName());
            if (worldConfig == null) return;
            PortalConfig portalConfig = worldConfig.endPortal();
            if (!portalConfig.enabled()) {
                event.setCancelled(true);
                return;
            }
            ListenersUtils.checkPermission(location, event.getPlayer(), PermissionsIsland.USE_END_PORTAL, event);
        } else if (teleportCause.equals(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL)) {
            Location location = event.getPlayer().getLocation();
            WorldConfig worldConfig = WorldUtils.getWorldConfig(location.getWorld().getName());
            if (worldConfig == null) return;
            PortalConfig portalConfig = worldConfig.netherPortal();
            if (!portalConfig.enabled()) {
                event.setCancelled(true);
                return;
            }
            ListenersUtils.checkPermission(location, event.getPlayer(), PermissionsIsland.USE_NETHER_PORTAL, event);
        }

    }
}
