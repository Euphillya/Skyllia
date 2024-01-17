package fr.euphyllia.skyfolia.listeners.bukkitevents;

import fr.euphyllia.skyfolia.api.InterneAPI;
import fr.euphyllia.skyfolia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyfolia.listeners.ListenersUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEvent;

public class PlayerEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(PlayerEvent.class);

    public PlayerEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerUseBucket(final PlayerBucketEvent event) {
        if (event.isCancelled()) return;
        ListenersUtils.checkPermission(event.getBlock().getChunk(), event.getPlayer(), PermissionsIsland.BUCKETS, event);
    }
}
