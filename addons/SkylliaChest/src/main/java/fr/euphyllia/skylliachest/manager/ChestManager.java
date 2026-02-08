package fr.euphyllia.skylliachest.manager;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.database.IslandCustomDataQuery;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliachest.SkylliaChest;
import fr.euphyllia.skylliachest.api.ChestIsland;
import fr.euphyllia.skylliachest.utils.InventoryDataType;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ChestManager {

    private static final String CHEST_DATA_KEY = "chest_contents";
    private static final String CHEST_SIZE_KEY = "chest_size";

    private final NamespacedKey namespaceKey;
    private final IslandCustomDataQuery customDataQuery;


    public ChestManager(@NotNull SkylliaChest plugin) {
        this.namespaceKey = new NamespacedKey(plugin, "island_chest");
        this.customDataQuery = SkylliaAPI.getIslandCustomDataQuery();
    }

    @NotNull
    public ChestIsland loadChest(@NotNull Island island, int size, @NotNull Component title) {
        ItemStack[] contents = customDataQuery.get(
                namespaceKey,
                island,
                CHEST_DATA_KEY,
                InventoryDataType.INSTANCE
        );

        Map<Integer, ItemStack> items = new HashMap<>();

        if (contents != null && contents.length > 0) {
            for (int i = 0; i < Math.min(contents.length, size); i++) {
                ItemStack item = contents[i];
                if (item != null && !item.getType().isAir()) {
                    items.put(i, item.clone());
                }
            }
        }
        return new ChestIsland(island, size, title, items);
    }

    public boolean saveChest(@NotNull ChestIsland chestIsland) {
        if (!chestIsland.isDirty()) {
            return true;
        }

        Island island = chestIsland.getIsland();
        ItemStack[] contents = chestIsland.toItemStackArray();

        boolean success = customDataQuery.set(
                namespaceKey,
                island,
                CHEST_DATA_KEY,
                InventoryDataType.INSTANCE,
                contents
        );

        if (success) {
            chestIsland.setDirty(false);
        }

        return success;
    }

    public int getChestSize(@NotNull Island island) {
        Integer savedSize = customDataQuery.get(
                namespaceKey,
                island,
                CHEST_SIZE_KEY,
                PersistentDataType.INTEGER
        );

        if (savedSize != null) {
            return normalizeChestSize(savedSize);
        }
        return 27;
    }

    public boolean setChestSize(@NotNull Island island, int size) {
        int normalizedSize = normalizeChestSize(size);
        return customDataQuery.set(
                namespaceKey,
                island,
                CHEST_SIZE_KEY,
                PersistentDataType.INTEGER,
                normalizedSize
        );
    }

    public boolean clearChest(@NotNull Island island) {
        return customDataQuery.remove(namespaceKey, island, CHEST_DATA_KEY);
    }

    private int normalizeChestSize(int size) {
        if (size <= 9) return 9;
        if (size <= 18) return 18;
        if (size <= 27) return 27;
        if (size <= 36) return 36;
        if (size <= 45) return 45;
        return 54;
    }
}
