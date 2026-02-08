package fr.euphyllia.skylliachest.inventory;

import fr.euphyllia.skylliachest.SkylliaChest;
import fr.euphyllia.skylliachest.api.ChestIsland;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class IslandChestInventory implements InventoryHolder {

    private final Inventory inventory;
    private final ChestIsland chestIsland;

    public IslandChestInventory(@NotNull ChestIsland chestIsland) {
        this.chestIsland = chestIsland;

        this.inventory = Bukkit.createInventory(
                this,
                chestIsland.getSize(),
                chestIsland.getTitle()
        );

        syncFromChestIsland();
    }

    private void syncFromChestIsland() {
        ItemStack[] contents = chestIsland.toItemStackArray();
        inventory.setContents(contents);
    }

    public void syncToChestIsland() {
        ItemStack @NotNull [] content = inventory.getContents();
        chestIsland.updateFromInventory(content);
    }

    @NotNull
    public ChestIsland getChestIsland() {
        return chestIsland;
    }

    public boolean save() {

        syncToChestIsland();

        return SkylliaChest.getInstance().getChestManager().saveChest(chestIsland);
    }


    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
