package fr.euphyllia.skylliachest.listeners;

import fr.euphyllia.skylliachest.SkylliaChest;
import fr.euphyllia.skylliachest.api.ChestIsland;
import fr.euphyllia.skylliachest.cache.ChestIslandCache;
import fr.euphyllia.skylliachest.inventory.IslandChestInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record ChestListener(SkylliaChest plugin) implements Listener {

    private static final Logger log = LoggerFactory.getLogger(ChestListener.class);

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        Inventory inventory = event.getInventory();

        if (!(inventory.getHolder(false) instanceof IslandChestInventory chestInventory)) {
            return;
        }

        chestInventory.syncToChestIsland();

        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
            ChestIslandCache cache = plugin.getChestCache();

            ChestIsland chestIsland = cache.unregisterPlayer(player);

            if (chestIsland != null && chestIsland.isDirty()) {
                boolean success = SkylliaChest.getInstance().getChestManager().saveChest(chestIsland);
                if (!success) {
                    log.error("Failed to save chest for island {} when player {} closed the inventory", chestIsland.getIsland().getId(), player.getUniqueId());
                }
            }
        });

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(final InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        final Inventory inventory = event.getInventory();

        if (!(inventory.getHolder(false) instanceof IslandChestInventory chestInventory)) {
            return;
        }

        chestInventory.syncToChestIsland();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(final InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        final Inventory inventory = event.getInventory();

        if (!(inventory.getHolder(false) instanceof IslandChestInventory chestInventory)) {
            return;
        }

        chestInventory.syncToChestIsland();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        ChestIslandCache cache = plugin.getChestCache();

        if (!cache.hasOpenChest(player)) {
            return;
        }

        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
            ChestIsland chestIsland = cache.getPlayerChest(player);

            ChestIsland removedChest = cache.unregisterPlayer(player);

            if (removedChest != null && removedChest.isDirty()) {
                boolean success = SkylliaChest.getInstance().getChestManager().saveChest(removedChest);
                if (!success) {
                    log.error("Failed to save chest for island {} when player {} quit", removedChest.getIsland().getId(), player.getUniqueId());
                }
            } else if (chestIsland != null) {
                if (chestIsland.isDirty()) {
                    boolean success = SkylliaChest.getInstance().getChestManager().saveChest(chestIsland);
                    if (!success) {
                        log.error("Failed to save chest for island {} when player {} quit", chestIsland.getIsland().getId(), player.getUniqueId());
                    }
                }
            }
        });
    }

}
