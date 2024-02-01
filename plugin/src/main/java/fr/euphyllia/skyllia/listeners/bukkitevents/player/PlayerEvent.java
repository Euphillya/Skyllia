package fr.euphyllia.skyllia.listeners.bukkitevents.player;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(PlayerEvent.class);

    public PlayerEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBucketEmptyEvent(PlayerBucketEmptyEvent event) {
        onPlayerUseBucket(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBucketFillEvent(PlayerBucketFillEvent event) {
        onPlayerUseBucket(event);
    }

    public void onPlayerUseBucket(final PlayerBucketEvent event) {
        if (event.isCancelled()) return;
        ListenersUtils.checkPermission(event.getBlock().getLocation(), event.getPlayer(), PermissionsIsland.BUCKETS, event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        if (event.isCancelled()) return;
        ListenersUtils.checkPermission(event.getItemDrop().getLocation(), event.getPlayer(), PermissionsIsland.DROP_ITEMS, event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickUpItemDropped(final EntityPickupItemEvent event) {
        if (event.isCancelled()) return;
        if (event.getEntity() instanceof Player player) {
            ListenersUtils.checkPermission(event.getItem().getLocation(), player, PermissionsIsland.PICKUP_ITEMS, event);
        }
    }

}
