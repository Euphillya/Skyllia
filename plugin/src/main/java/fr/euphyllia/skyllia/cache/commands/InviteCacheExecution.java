package fr.euphyllia.skyllia.cache.commands;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class InviteCacheExecution {

    private static final Cache<UUID, Set<UUID>> INVITE_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(5000)
            .build();

    public static boolean isInvitedCache(UUID islandId, UUID playerId) {
        Set<UUID> invitedPlayers = INVITE_CACHE.getIfPresent(islandId);
        return (invitedPlayers != null && invitedPlayers.contains(playerId));
    }

    public static void addInviteCache(UUID islandId, UUID playerId) {
        INVITE_CACHE.asMap().compute(islandId, (key, oldSet) -> {
            if (oldSet == null) {
                oldSet = new HashSet<>();
            }
            oldSet.add(playerId);
            return oldSet;
        });
    }

    public static void removeInviteCache(UUID islandId, UUID playerId) {
        INVITE_CACHE.asMap().computeIfPresent(islandId, (key, oldSet) -> {
            oldSet.remove(playerId);
            if (oldSet.isEmpty()) return null;
            return oldSet;
        });
    }

    public static void invalidateAll() {
        INVITE_CACHE.invalidateAll();
    }
}
