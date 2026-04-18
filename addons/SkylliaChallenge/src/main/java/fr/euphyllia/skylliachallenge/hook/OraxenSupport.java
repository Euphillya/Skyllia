package fr.euphyllia.skylliachallenge.hook;

import fr.euphyllia.skylliachallenge.api.CustomItemSupport;
import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import io.th0rgal.oraxen.mechanics.Mechanic;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class OraxenSupport implements CustomItemSupport {
    @Override
    public String getNamespace() {
        return "oraxen";
    }

    @Override
    public boolean matches(ItemStack item, String id) {
        if (item == null || item.getType().isAir()) return false;
        if (id == null || id.isEmpty()) return false;
        if (OraxenItems.getIdByItem(item) == null) return false;
        return OraxenItems.getIdByItem(item).equalsIgnoreCase(id);
    }

    @Override
    public boolean matchesBlock(Block block, String id) {
        if (block == null || block.getType().isAir()) return false;
        if (id == null || id.isEmpty()) return false;
        Mechanic mechanic = OraxenBlocks.getOraxenBlock(block.getBlockData());
        if (mechanic == null) return false;
        return mechanic.getItemID().equalsIgnoreCase(id);
    }

    @Override
    public ItemStack getItemFromId(String id) {
        if (id == null || id.isEmpty()) return null;
        ItemBuilder item = OraxenItems.getItemById(id);
        return item.build();
    }
}