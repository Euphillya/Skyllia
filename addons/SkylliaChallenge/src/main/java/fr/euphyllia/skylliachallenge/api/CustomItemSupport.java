package fr.euphyllia.skylliachallenge.api;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public interface CustomItemSupport {

    String getNamespace();

    boolean matches(ItemStack item, String id);

    boolean matchesBlock(Block block, String id);

    ItemStack getItemFromId(String id);
}
