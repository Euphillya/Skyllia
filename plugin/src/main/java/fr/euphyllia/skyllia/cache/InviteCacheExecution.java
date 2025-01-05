package fr.euphyllia.skyllia.cache;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InviteCacheExecution {

    private static final ConcurrentHashMap<UUID, CopyOnWriteArrayList<UUID>> invitePending = new ConcurrentHashMap<>();

    public static synchronized boolean isInvitedCache(UUID islandId, UUID playerId) {
        return invitePending.computeIfAbsent(islandId, k -> new CopyOnWriteArrayList<>()).contains(playerId);
    }

    public static synchronized void addInviteCache(UUID islandId, UUID playerId) {
        invitePending.computeIfAbsent(islandId, k -> new CopyOnWriteArrayList<>()).add(playerId);
    }

    public static synchronized void removeInviteCache(UUID islandId, UUID playerId) {
        invitePending.computeIfAbsent(islandId, k -> new CopyOnWriteArrayList<>()).remove(playerId);
    }
}
