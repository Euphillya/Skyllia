package fr.euphyllia.skyllia.listeners.bukkitevents.player;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.configuration.PortalConfig;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
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
