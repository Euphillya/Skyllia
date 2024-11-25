package fr.euphyllia.skyllia.listeners.bukkitevents.player;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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

        Block block = event.getClickedBlock();
        if (block != null) {
            Material material = block.getType();

            if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK)
                    && (material == Material.COMPARATOR || material == Material.REPEATER)) {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsIsland.REDSTONE, event);
                return;
            }

            if (event.getAction() == Action.PHYSICAL
                    && (material == Material.TRIPWIRE
                    || material.name().contains("PRESSURE_PLATE"))) {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsIsland.REDSTONE, event);
                return;
            }
        }

        ListenersUtils.checkPermission(player.getLocation(), player, PermissionsIsland.INTERACT, event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntitiesEvent(final PlayerInteractEntityEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        if (player.hasPermission("skyllia.interact_entity.bypass")) {
            return;
        }
        ListenersUtils.checkPermission(event.getRightClicked().getLocation(), player, PermissionsIsland.INTERACT_ENTITIES, event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntitiesEvent(final PlayerInteractAtEntityEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        if (player.hasPermission("skyllia.interact_entity.bypass")) {
            return;
        }
        ListenersUtils.checkPermission(event.getRightClicked().getLocation(), player, PermissionsIsland.INTERACT_ENTITIES, event);
    }

}
