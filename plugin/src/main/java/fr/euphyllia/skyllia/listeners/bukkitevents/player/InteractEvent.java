package fr.euphyllia.skyllia.listeners.bukkitevents.player;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractEvent implements Listener {
    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(InteractEvent.class);

    public InteractEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEvent(final PlayerInteractEvent event) {
        if (event.useInteractedBlock().equals(Event.Result.DENY)) return;
        if (event.useItemInHand().equals(Event.Result.DENY)) return;

        Player player = event.getPlayer();
        if (player.hasPermission("skyllia.interact.bypass")) {
            return;
        }
        ListenersUtils.checkPermission(player.getChunk(), player, PermissionsIsland.INTERACT, event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntitiesEvent(final PlayerInteractEntityEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        if (player.hasPermission("skyllia.interact_entity.bypass")) {
            return;
        }
        ListenersUtils.checkPermission(event.getRightClicked().getChunk(), player, PermissionsIsland.INTERACT_ENTITIES, event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntitiesEvent(final PlayerInteractAtEntityEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        if (player.hasPermission("skyllia.interact_entity.bypass")) {
            return;
        }
        ListenersUtils.checkPermission(event.getRightClicked().getChunk(), player, PermissionsIsland.INTERACT_ENTITIES, event);
    }

}
