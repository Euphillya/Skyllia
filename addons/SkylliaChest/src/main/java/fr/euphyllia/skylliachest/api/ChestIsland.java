package fr.euphyllia.skylliachest.api;

import fr.euphyllia.skyllia.api.skyblock.Island;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChestIsland {

    private final int size;
    private final Component title;
    private final Island island;
    private final Map<Integer, ItemStack> itemsIndexed;
    private boolean dirty;

    public ChestIsland(Island island, int size, Component title, Map<Integer, ItemStack> itemsContent) {
        this.island = island;
        this.size = size;
        this.title = title;
        this.itemsIndexed = itemsContent;
        this.dirty = false;
    }

    @NotNull
    public Island getIsland() {
        return island;
    }

    public int getSize() {
        return size;
    }

    @NotNull
    public Component getTitle() {
        return title;
    }

    @NotNull
    public Map<Integer, ItemStack> getItemsIndexed() {
        return new ConcurrentHashMap<>(itemsIndexed);
    }

    public void setItem(int index, ItemStack item) {
        if (index < 0 || index >= size) {
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }

        if (item == null || item.getType().isAir()) {
            itemsIndexed.remove(index);
        } else {
            itemsIndexed.put(index, item.clone());
        }
        this.dirty = true;
    }

    public ItemStack getItem(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        ItemStack item = itemsIndexed.get(index);
        return item != null ? item.clone() : null;
    }

    public void updateFromInventory(@NotNull ItemStack[] contents) {
        itemsIndexed.clear();
        for (int i = 0; i < Math.min(contents.length, size); i++) {
            ItemStack item = contents[i];
            if (!item.getType().isAir()) {
                itemsIndexed.put(i, item.clone());
            }
        }
        this.dirty = true;
    }

    @NotNull
    public ItemStack[] toItemStackArray() {
        ItemStack[] array = new ItemStack[size];
        for (Map.Entry<Integer, ItemStack> entry : itemsIndexed.entrySet()) {
            Integer index = entry.getKey();
            ItemStack item = entry.getValue();
            if (index < size) {
                array[index] = item.clone();
            }
        }
        return array;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean state) {
        this.dirty = state;
    }

    public void clear() {
        itemsIndexed.clear();
        this.dirty = true;
    }

    public int getOccupiedSlots() {
        return itemsIndexed.size();
    }

    public boolean isEmpty() {
        return itemsIndexed.isEmpty();
    }
}
