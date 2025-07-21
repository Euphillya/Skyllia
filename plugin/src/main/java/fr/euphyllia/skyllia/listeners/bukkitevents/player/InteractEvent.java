package fr.euphyllia.skyllia.listeners.bukkitevents.player;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class InteractEvent implements Listener {
    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(InteractEvent.class);

    public InteractEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerConvertObsidianToLava(final PlayerInteractEvent event) {
        if (!ConfigLoader.general.isEnableObsidianToLavaConversion()) return;
        if (event.useInteractedBlock() == Event.Result.DENY || event.useItemInHand() == Event.Result.DENY) return;

        final Player player = event.getPlayer();
        if (PermissionImp.hasPermission(player, "skyllia.interact.obsidian_convert_bypass")) return; // Le Bypass standard empêche de convertir l'obsidienne en lave.

        final Block block = event.getClickedBlock();
        final EquipmentSlot hand = event.getHand();
        if (block == null || hand == null || block.getType() != Material.OBSIDIAN) return;
        if (!event.getAction().isRightClick()) return;

        ListenersUtils.checkPermission(block.getLocation(), player, PermissionsIsland.INTERACT, event);
        if (event.isCancelled()) return; // Check if the permission check cancelled the event

        player.getScheduler().runDelayed(Skyllia.getInstance(), scheduledTask -> {
            ItemStack handItem = switch (hand) {
                case HAND -> player.getInventory().getItemInMainHand();
                case OFF_HAND -> player.getInventory().getItemInOffHand();
                default -> null;
            };
            if (handItem == null || handItem.getType() != Material.BUCKET) return;

            block.setType(Material.AIR);

            handItem.setAmount(Math.max(0, handItem.getAmount() - 1));

            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(new ItemStack(Material.LAVA_BUCKET));
            leftovers.values().forEach(leftover ->
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover)
            );

            player.swingHand(hand);
            player.playSound(block.getLocation(), org.bukkit.Sound.ITEM_BUCKET_FILL_LAVA, 1.0f, 1.0f);
        }, null, 1L);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractEvent(final PlayerInteractEvent event) {
        if (event.useInteractedBlock().equals(Event.Result.DENY)) return;
        if (event.useItemInHand().equals(Event.Result.DENY)) return;

        Player player = event.getPlayer();

        if (PermissionImp.hasPermission(player, "skyllia.interact.bypass")) {
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

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractEntitiesEvent(final PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (PermissionImp.hasPermission(player, "skyllia.interact_entity.bypass")) {
            return;
        }
        ListenersUtils.checkPermission(event.getRightClicked().getLocation(), player, PermissionsIsland.INTERACT_ENTITIES, event);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractEntitiesEvent(final PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        if (PermissionImp.hasPermission(player, "skyllia.interact_entity.bypass")) {
            return;
        }
        ListenersUtils.checkPermission(event.getRightClicked().getLocation(), player, PermissionsIsland.INTERACT_ENTITIES, event);
    }

}
