package fr.euphyllia.skylliachallenge.hook;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoItems;
import fr.euphyllia.skylliachallenge.api.CustomItemSupport;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class NexoSupport implements CustomItemSupport {

    @Override
    public String getNamespace() {
        return "nexo";
    }

    @Override
    public boolean matches(ItemStack item, String id) {
        if (item == null || item.getType().isAir()) return false;
        if (id == null || id.isEmpty()) return false;
        String itemId = NexoItems.idFromItem(item);
        if (itemId == null) return false;
        return itemId.equalsIgnoreCase(id);
    }

    @Override
    public boolean matchesBlock(Block block, String id) {
        if (block == null || block.getType().isAir()) return false;
        if (id == null || id.isEmpty()) return false;
        com.nexomc.nexo.mechanics.Mechanic mechanic = NexoBlocks.stringMechanic(block.getBlockData());
        if (mechanic == null) return false;
        return mechanic.getItemID().equalsIgnoreCase(id);
    }

    @Override
    public ItemStack getItemFromId(String id) {
        if (id == null || id.isEmpty()) return null;
        com.nexomc.nexo.items.ItemBuilder item = NexoItems.itemFromId(id);
        if (item == null) return null;
        return item.build();
    }
}