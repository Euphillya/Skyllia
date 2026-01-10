package fr.euphyllia.skyllia.cache;

import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TrustService {

    private final ConcurrentHashMap<UUID, Set<UUID>> trustedByIsland = new ConcurrentHashMap<>();

    public boolean addTrusted(UUID islandId, UUID playerId) {
        return trustedByIsland
                .computeIfAbsent(islandId, __ -> ConcurrentHashMap.newKeySet())
                .add(playerId);
    }

    public boolean removeTrusted(UUID islandId, UUID playerId) {
        Set<UUID> set = trustedByIsland.get(islandId);
        if (set == null) return false;
        boolean removed = set.remove(playerId);
        if (set.isEmpty()) trustedByIsland.remove(islandId, set);
        return removed;
    }

    public boolean isTrusted(UUID islandId, UUID playerId) {
        Set<UUID> set = trustedByIsland.get(islandId);
        return set != null && set.contains(playerId);
    }

    public @Nullable Set<UUID> getTrusted(UUID islandId) {
        Set<UUID> set = trustedByIsland.get(islandId);
        return set != null ? Set.copyOf(set) : null;
    }

    public void clearIsland(UUID islandId) {
        trustedByIsland.remove(islandId);
    }

    public void clearAll() {
        trustedByIsland.clear();
    }
}
