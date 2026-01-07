package fr.euphyllia.skyllia.listeners.bukkitevents.player;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.PermissionImp;
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
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;

public class InventoryEvent implements Listener {
    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(InventoryEvent.class);

    public InventoryEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInventoryOpen(final InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        if (PermissionImp.hasPermission(player, "skyllia.interact.bypass")) return;
        InventoryType inventoryType = event.getInventory().getType();
        switch (inventoryType) {
            case MERCHANT -> {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsInventory.OPEN_MERCHANT, event);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractEvent(final PlayerInteractEvent event) {
        if (event.useInteractedBlock() == Event.Result.DENY) return;
        Player player = event.getPlayer();
        if (PermissionImp.hasPermission(player, "skyllia.interact.bypass")) return;
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        Material inventoryType = clickedBlock.getType();
        switch (inventoryType) {
            case CHEST, TRAPPED_CHEST -> {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsInventory.OPEN_CHEST, event);
            }
            case DISPENSER -> {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsInventory.OPEN_DISPENSER, event);
            }
            case DROPPER -> {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsInventory.OPEN_DROPPER, event);
            }
            case FURNACE -> {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsInventory.OPEN_FURNACE, event);
            }
            case CRAFTING_TABLE -> {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsInventory.OPEN_WORKBENCH, event);
            }
            case ENCHANTING_TABLE -> {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsInventory.OPEN_ENCHANTING, event);
            }
            case BREWING_STAND -> {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsInventory.OPEN_BREWING, event);
            }
            case ANVIL -> {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsInventory.OPEN_ANVIL, event);
            }
            case SMITHING_TABLE -> {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsInventory.OPEN_SMITHING, event);
            }
            case BEACON -> {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsInventory.OPEN_BEACON, event);
            }
            case HOPPER -> {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsInventory.OPEN_HOPPER, event);
            }
            case SHULKER_BOX, WHITE_SHULKER_BOX, ORANGE_SHULKER_BOX, MAGENTA_SHULKER_BOX, LIGHT_BLUE_SHULKER_BOX,
                 YELLOW_SHULKER_BOX, LIME_SHULKER_BOX, PINK_SHULKER_BOX, GRAY_SHULKER_BOX, LIGHT_GRAY_SHULKER_BOX,
                 CYAN_SHULKER_BOX, PURPLE_SHULKER_BOX, BLUE_SHULKER_BOX, BROWN_SHULKER_BOX, GREEN_SHULKER_BOX,
                 RED_SHULKER_BOX, BLACK_SHULKER_BOX -> {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsInventory.OPEN_SHULKER_BOX, event);
            }
            case BARREL -> {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsInventory.OPEN_BARREL, event);
            }
            case BLAST_FURNACE -> {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsInventory.OPEN_BLAST_FURNACE, event);
            }
            case LECTERN -> {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsInventory.OPEN_LECTERN, event);
            }
            case SMOKER -> {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsInventory.OPEN_SMOKER, event);
            }
            case LOOM -> {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsInventory.OPEN_LOOM, event);
            }
            case CARTOGRAPHY_TABLE -> {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsInventory.OPEN_CARTOGRAPHY, event);
            }
            case GRINDSTONE -> {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsInventory.OPEN_GRINDSTONE, event);
            }
            case STONECUTTER -> {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsInventory.OPEN_STONECUTTER, event);
            }
            case CRAFTER -> {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsInventory.OPEN_CRAFTER, event);
            }
            default -> {
            }
        }
    }
}
