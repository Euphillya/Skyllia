package fr.euphyllia.skyllia.listeners.bukkitevents.player;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsInventory;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

public class InventoryEvent implements Listener {
    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(InventoryEvent.class);

    public InventoryEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryOpen(final InventoryOpenEvent event) {
        if (event.isCancelled()) return;
        Player player = (Player) event.getPlayer();
        InventoryType inventoryType = event.getInventory().getType();
        switch (inventoryType) {
            case CHEST -> {
                ListenersUtils.checkPermission(player.getChunk(), player, PermissionsInventory.OPEN_CHEST, event);
            }
            case DISPENSER -> {
                ListenersUtils.checkPermission(player.getChunk(), player, PermissionsInventory.OPEN_DISPENSER, event);
            }
            case DROPPER -> {
                ListenersUtils.checkPermission(player.getChunk(), player, PermissionsInventory.OPEN_DROPPER, event);
            }
            case FURNACE -> {
                ListenersUtils.checkPermission(player.getChunk(), player, PermissionsInventory.OPEN_FURNACE, event);
            }
            case WORKBENCH -> {
                ListenersUtils.checkPermission(player.getChunk(), player, PermissionsInventory.OPEN_WORKBENCH, event);
            }
            case ENCHANTING -> {
                ListenersUtils.checkPermission(player.getChunk(), player, PermissionsInventory.OPEN_ENCHANTING, event);
            }
            case BREWING -> {
                ListenersUtils.checkPermission(player.getChunk(), player, PermissionsInventory.OPEN_BREWING, event);
            }
            case MERCHANT -> {
                ListenersUtils.checkPermission(player.getChunk(), player, PermissionsInventory.OPEN_MERCHANT, event);
            }
            case ANVIL -> {
                ListenersUtils.checkPermission(player.getChunk(), player, PermissionsInventory.OPEN_ANVIL, event);
            }
            case SMITHING -> {
                ListenersUtils.checkPermission(player.getChunk(), player, PermissionsInventory.OPEN_SMITHING, event);
            }
            case BEACON -> {
                ListenersUtils.checkPermission(player.getChunk(), player, PermissionsInventory.OPEN_BEACON, event);
            }
            case HOPPER -> {
                ListenersUtils.checkPermission(player.getChunk(), player, PermissionsInventory.OPEN_HOPPER, event);
            }
            case SHULKER_BOX -> {
                ListenersUtils.checkPermission(player.getChunk(), player, PermissionsInventory.OPEN_SHULKER_BOX, event);
            }
            case BARREL -> {
                ListenersUtils.checkPermission(player.getChunk(), player, PermissionsInventory.OPEN_BARREL, event);
            }
            case BLAST_FURNACE -> {
                ListenersUtils.checkPermission(player.getChunk(), player, PermissionsInventory.OPEN_BLAST_FURNACE, event);
            }
            case LECTERN -> {
                ListenersUtils.checkPermission(player.getChunk(), player, PermissionsInventory.OPEN_LECTERN, event);
            }
            case SMOKER -> {
                ListenersUtils.checkPermission(player.getChunk(), player, PermissionsInventory.OPEN_SMOKER, event);
            }
            case LOOM -> {
                ListenersUtils.checkPermission(player.getChunk(), player, PermissionsInventory.OPEN_LOOM, event);
            }
            case CARTOGRAPHY -> {
                ListenersUtils.checkPermission(player.getChunk(), player, PermissionsInventory.OPEN_CARTOGRAPHY, event);
            }
            case GRINDSTONE -> {
                ListenersUtils.checkPermission(player.getChunk(), player, PermissionsInventory.OPEN_GRINDSTONE, event);
            }
            case STONECUTTER -> {
                ListenersUtils.checkPermission(player.getChunk(), player, PermissionsInventory.OPEN_STONECUTTER, event);
            }
            case SMITHING_NEW -> {
                ListenersUtils.checkPermission(player.getChunk(), player, PermissionsInventory.OPEN_SMITHING_NEW, event);
            }
            default -> {
                // TODO sera remplace plus tard
                if (inventoryType.name().equalsIgnoreCase("CRAFTER")) {
                    ListenersUtils.checkPermission(player.getChunk(), player, PermissionsInventory.OPEN_CRAFTER, event);
                }
            }
        }
    }
}
