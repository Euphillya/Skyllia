package fr.euphyllia.skyllia.cache.commands;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InviteCacheExecution {

    private static final Map<UUID, Set<UUID>> INVITE_CACHE = new ConcurrentHashMap<>();

    public static boolean isInvitedCache(UUID islandId, UUID playerId) {
        Set<UUID> invitedPlayers = INVITE_CACHE.getOrDefault(islandId, null);
        return (invitedPlayers != null && invitedPlayers.contains(playerId));
    }

    public static void addInviteCache(UUID islandId, UUID playerId) {
        INVITE_CACHE.compute(islandId, (key, oldSet) -> {
            if (oldSet == null) {
                oldSet = ConcurrentHashMap.newKeySet();
            }
            oldSet.add(playerId);
            return oldSet;
        });
    }

    public static void removeInviteCache(UUID islandId, UUID playerId) {
        INVITE_CACHE.computeIfPresent(islandId, (key, oldSet) -> {
            oldSet.remove(playerId);
            if (oldSet.isEmpty()) return null;
            return oldSet;
        });
    }
}
