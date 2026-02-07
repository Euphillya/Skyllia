package fr.euphyllia.skylliachest.inventory;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class IslandChestInventory implements InventoryHolder {

    private final Inventory inventory;
    private final Island island;

    public IslandChestInventory(@NotNull Island island, Player player, int size) {
        this.island = island;
        Component title = ConfigLoader.language.translate(player, "addons.island-chest.inventory-title", Map.of("%island_name%", island.getOwner().getLastKnowName()));
        this.inventory = Bukkit.createInventory(this, size, title);
    }
    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
