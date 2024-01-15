package fr.euphyllia.skyfolia.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InviteCacheExecution {

    private static final ConcurrentHashMap<UUID, List<UUID>> invitePending = new ConcurrentHashMap<>();

    public static boolean isInvitedCache(UUID islandId, UUID playerId) {
        List<UUID> playerIdList = invitePending.getOrDefault(islandId, null);
        if (playerIdList == null) return false;
        return playerIdList.contains(playerId);
    }

    public static void addInviteCache(UUID islandId, UUID playerId) {
        if (isInvitedCache(islandId, playerId)) return;
        List<UUID> playerIdList = invitePending.getOrDefault(islandId, new ArrayList<>());
        playerIdList.add(playerId);
        invitePending.put(islandId, playerIdList);
    }

    public static void removeInviteCache(UUID islandId, UUID playerId) {
        if (isInvitedCache(islandId, playerId)) {
            List<UUID> playerIdList = invitePending.getOrDefault(islandId, new ArrayList<>());
            playerIdList.remove(playerId);
            invitePending.put(islandId, playerIdList);
        }
    }

    public static List<UUID> getInvitedListCache(UUID islandId) {
        return invitePending.getOrDefault(islandId, new ArrayList<>());
    }
}
