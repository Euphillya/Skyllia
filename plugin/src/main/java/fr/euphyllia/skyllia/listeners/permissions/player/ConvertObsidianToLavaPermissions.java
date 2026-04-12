package fr.euphyllia.skyllia.listeners.permissions.player;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.PermissionModule;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

public class ConvertObsidianToLavaPermissions implements PermissionModule {

    private PermissionId CONVERT_OBSIDIAN_TO_LAVA;

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInteractEntity(final PlayerInteractEvent event) {
        if (!ConfigLoader.general.isEnableObsidianToLavaConversion()) return;
        if (event.useInteractedBlock() == Event.Result.DENY || event.useItemInHand() == Event.Result.DENY) return;
        if (!event.getAction().isRightClick()) return;

        final Player player = event.getPlayer();
        final Block target = event.getClickedBlock();
        if (target == null || target.getType() != Material.OBSIDIAN) return;

        final Location location = target.getLocation();

        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;

        final int chunkX = location.getBlockX() >> 4;
        final int chunkZ = location.getBlockZ() >> 4;
        final Island island = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
        if (island == null) return;

        final boolean hasPermission = SkylliaAPI.getPermissionsManager().hasPermission(player, island, CONVERT_OBSIDIAN_TO_LAVA, "skyllia.player.convert_obsidian_to_lava.bypass");
        if (!hasPermission) {
            event.setCancelled(true);
            return;
        }

        final EquipmentSlot hand = event.getHand();
        if (hand == null) return;

        ItemStack handItem = switch (hand) {
            case HAND -> player.getInventory().getItemInMainHand();
            case OFF_HAND -> player.getInventory().getItemInOffHand();
            default -> null;
        };
        if (handItem == null || handItem.getType() != Material.BUCKET) return;

        target.setType(Material.AIR);

        handItem.setAmount(Math.max(0, handItem.getAmount() - 1));

        HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(new ItemStack(Material.LAVA_BUCKET));
        leftovers.values().forEach(leftover ->
                player.getWorld().dropItemNaturally(player.getLocation(), leftover)
        );

        player.swingHand(hand);
        player.playSound(target.getLocation(), org.bukkit.Sound.ITEM_BUCKET_FILL_LAVA, 1.0f, 1.0f);
    }

    @Override
    public void registerPermissions(PermissionRegistry registry, Plugin owner) {
        this.CONVERT_OBSIDIAN_TO_LAVA = registry.register(new PermissionNode(
                new NamespacedKey(owner, "player.convert_obsidian_to_lava"),
                "island.permission.convert_obsidian_to_lava.name",
                "island.permission.convert_obsidian_to_lava.description"
        ));
    }
}
