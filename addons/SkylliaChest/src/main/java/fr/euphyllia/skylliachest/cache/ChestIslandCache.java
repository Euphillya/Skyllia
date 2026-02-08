package fr.euphyllia.skylliachest.cache;

import fr.euphyllia.skylliachest.api.ChestIsland;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChestIslandCache {

    private final Map<UUID, ChestIsland> cachedChests = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> playerOpenChests = new ConcurrentHashMap<>();

    public void registerOpenChest(@NotNull Player player, @NotNull ChestIsland chestIsland) {
        UUID islandId = chestIsland.getIsland().getId();

        cachedChests.putIfAbsent(islandId, chestIsland);

        playerOpenChests.put(player.getUniqueId(), islandId);
    }

    @Nullable
    public ChestIsland getCachedChest(@NotNull UUID islandId) {
        return cachedChests.get(islandId);
    }

    @Nullable
    public ChestIsland getPlayerChest(@NotNull Player player) {
        UUID islandId = playerOpenChests.get(player.getUniqueId());
        return islandId != null ? cachedChests.get(islandId) : null;
    }

    @Nullable
    public UUID getPlayerOpenChestIsland(@NotNull Player player) {
        return playerOpenChests.get(player.getUniqueId());
    }

    @Nullable
    public ChestIsland unregisterPlayer(@NotNull Player player) {
        UUID islandId = playerOpenChests.remove(player.getUniqueId());
        if (islandId == null) {
            return null;
        }

        boolean stillInUse = playerOpenChests.containsValue(islandId);
        if (!stillInUse) {
            return cachedChests.remove(islandId);
        }
        return null;
    }

    public boolean hasOpenChest(@NotNull Player player) {
        return playerOpenChests.containsKey(player.getUniqueId());
    }

    public long countPlayersWithChest(@NotNull UUID islandId) {
        long count = 0L;
        for (UUID id : playerOpenChests.values()) {
            if (id.equals(islandId)) {
                count++;
            }
        }
        return count;
    }

    @NotNull
    public Map<UUID, ChestIsland> getAllCachedChests() {
        return Map.copyOf(cachedChests);
    }

    public void clear() {
        cachedChests.clear();
        playerOpenChests.clear();
    }

    public void putChest(@NotNull ChestIsland chestIsland) {
        cachedChests.put(chestIsland.getIsland().getId(), chestIsland);
    }

    @Nullable
    public ChestIsland removeChest(@NotNull UUID islandId) {
        return cachedChests.remove(islandId);
    }
}
