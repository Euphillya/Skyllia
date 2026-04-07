package fr.euphyllia.skylliachest.commands;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliachest.SkylliaChest;
import fr.euphyllia.skylliachest.api.ChestIsland;
import fr.euphyllia.skylliachest.cache.ChestIslandCache;
import fr.euphyllia.skylliachest.inventory.IslandChestInventory;
import fr.euphyllia.skylliachest.manager.ChestManager;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChestCommand implements SubCommandInterface {

    private final Plugin plugin;
    private final PermissionId OPEN_ISLAND_CHEST_PERMISSION;

    public ChestCommand(Plugin plugin) {
        this.plugin = plugin;
        this.OPEN_ISLAND_CHEST_PERMISSION = SkylliaAPI.getPermissionRegistry().idOrRegister(
                new PermissionNode(
                        new NamespacedKey(plugin, "open_private_island_chest"),
                        "addons.chest.permission.open.name",
                        "addons.chest.permission.open.description"
                )
        );
    }

    @Override
    public void onExecute(@NotNull Plugin plugin, @NotNull CommandSender sender, @NonNull @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return;
        }

        UUID playerId = player.getUniqueId();

        Island island = SkylliaAPI.getIslandByPlayerId(playerId);
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "island.player.no-island");
            return;
        }

        boolean isAllowed = SkylliaAPI.getPermissionsManager().hasPermission(player, island, OPEN_ISLAND_CHEST_PERMISSION);
        if (!isAllowed) {
            ConfigLoader.language.sendMessage(player, "addons.island-chest.no-permission");
            return;
        }

        ChestManager chestManager = SkylliaChest.getInstance().getChestManager();
        chestManager.initIsland(island);

        int size = chestManager.getChestSize(island);

        openChest(player, island, size);
        return;
    }

    private void openChest(@NotNull Player player, @NotNull Island island, int size) {
        UUID islandId = island.getId();
        UUID playerId = player.getUniqueId();
        ChestIslandCache cache = SkylliaChest.getInstance().getChestCache();

        Component title = ConfigLoader.language.translate(
                player,
                "addons.island-chest.inventory-title",
                Map.of("%island_name%", island.getOwner().getLastKnowName())
        );

        ChestIsland chestIsland = cache.getCachedChest(islandId);

        if (chestIsland == null) {
            ChestManager chestManager = SkylliaChest.getInstance().getChestManager();
            chestIsland = chestManager.loadChest(island, size, title);
            cache.putChest(chestIsland);
        }

        IslandChestInventory inventory = cache.getCachedInventory(islandId);
        if (inventory == null) {
            inventory = new IslandChestInventory(chestIsland);
            cache.putInventory(islandId, inventory);
        }

        cache.registerOpenChest(playerId, chestIsland);

        final IslandChestInventory finalInventory = inventory;
        player.getScheduler().run(plugin, scheduledTask -> {
            player.openInventory(finalInventory.getInventory());
            ConfigLoader.language.sendMessage(player, "addons.island-chest.open");
        }, null);
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NonNull @NotNull String[] args) {
        return List.of();
    }
}
