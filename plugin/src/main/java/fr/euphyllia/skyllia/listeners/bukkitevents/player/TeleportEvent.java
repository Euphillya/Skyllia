package fr.euphyllia.skyllia.listeners.bukkitevents.player;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.event.PlayerPrepareChangeWorldSkyblockEvent;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try {
            executor.execute(() -> {
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
        } finally {
            executor.shutdown();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerUsePortal(final PlayerPortalEvent event) {
        if (event.isCancelled()) return;
        event.setCanCreatePortal(false);
        PlayerTeleportEvent.TeleportCause teleportCause = event.getCause();
        if (teleportCause.equals(PlayerTeleportEvent.TeleportCause.END_PORTAL)) {
            ListenersUtils.checkPermission(event.getPlayer().getLocation(), event.getPlayer(), PermissionsIsland.USE_END_PORTAL, event);
        } else if (teleportCause.equals(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL)) {
            ListenersUtils.checkPermission(event.getPlayer().getLocation(), event.getPlayer(), PermissionsIsland.USE_NETHER_PORTAL, event);
        }

    }
}
