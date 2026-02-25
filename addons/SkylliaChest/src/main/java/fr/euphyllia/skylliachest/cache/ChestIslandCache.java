package fr.euphyllia.skylliachest.cache;

import fr.euphyllia.skylliachest.api.ChestIsland;
import fr.euphyllia.skylliachest.inventory.IslandChestInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChestIslandCache {

    private final Map<UUID, ChestIsland> cachedChests = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> playerOpenChests = new ConcurrentHashMap<>();
    private final Map<UUID, IslandChestInventory> cachedInventories = new ConcurrentHashMap<>();

    public void registerOpenChest(@NotNull UUID playerId, @NotNull ChestIsland chestIsland) {
        UUID islandId = chestIsland.getIsland().getId();

        cachedChests.putIfAbsent(islandId, chestIsland);

        playerOpenChests.put(playerId, islandId);
    }

    @Nullable
    public IslandChestInventory getCachedInventory(@NotNull UUID islandId) {
        return cachedInventories.get(islandId);
    }

    public void putInventory(@NotNull UUID islandId, @NotNull IslandChestInventory inventory) {
        cachedInventories.put(islandId, inventory);
    }

    @Nullable
    public ChestIsland getCachedChest(@NotNull UUID islandId) {
        return cachedChests.get(islandId);
    }

    @Nullable
    public ChestIsland getPlayerChest(@NotNull UUID playerId) {
        UUID islandId = playerOpenChests.get(playerId);
        return islandId != null ? cachedChests.get(islandId) : null;
    }

    @Nullable
    public ChestIsland unregisterPlayer(@NotNull UUID playerId) {
        UUID islandId = playerOpenChests.remove(playerId);
        if (islandId == null) {
            return null;
        }

        boolean stillInUse = playerOpenChests.containsValue(islandId);
        if (!stillInUse) {
            cachedInventories.remove(islandId);
            return cachedChests.remove(islandId);
        }
        return null;
    }

    public boolean hasOpenChest(@NotNull UUID playerId) {
        return playerOpenChests.containsKey(playerId);
    }

    @NotNull
    public Map<UUID, ChestIsland> getAllCachedChests() {
        return Map.copyOf(cachedChests);
    }

    public void clear() {
        cachedChests.clear();
        playerOpenChests.clear();
        cachedInventories.clear();
    }

    public void putChest(@NotNull ChestIsland chestIsland) {
        cachedChests.put(chestIsland.getIsland().getId(), chestIsland);
    }
}
