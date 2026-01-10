package fr.euphyllia.skyllia.cache;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.api.utils.ExpiringValue;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SkyblockCache {

    private static final long TTL_STATE_SEC = 5;
    private static final long TTL_MEMBERS_SEC = 5;
    private static final long TTL_WARPS_SEC = 5;
    private static final long TTL_ISLAND_SEC = 60;
    private static final long TTL_PLAYER_LINK_SEC = 10;
    private static final long TTL_ROLE_SEC = 5;
    private static final long TTL_NAME_ROLE_SEC = 5;

    private final ConcurrentHashMap<UUID, ExpiringValue<Island>> islandById = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, ExpiringValue<UUID>> islandIdByPlayer = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<UUID, ExpiringValue<Players>> ownerByIsland = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, ExpiringValue<List<Players>>> membersByIsland = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, ExpiringValue<List<Players>>> bannedByIsland = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<MemberKey, ExpiringValue<RoleType>> roleByMember = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<MemberNameKey, ExpiringValue<RoleType>> roleByMemberName = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<UUID, ExpiringValue<List<WarpIsland>>> warpsByIsland = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<WarpKey, ExpiringValue<WarpIsland>> warpByKey = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<UUID, ExpiringValue<IslandStateSnapshot>> stateByIsland = new ConcurrentHashMap<>();

    private static <K, V> @Nullable V getIfValid(ConcurrentHashMap<K, ExpiringValue<V>> map, K key) {
        ExpiringValue<V> ev = map.get(key);
        if (ev == null) return null;
        if (ev.isExpired()) {
            map.remove(key, ev);
            return null;
        }
        return ev.get();
    }

    private static <K, V> void put(ConcurrentHashMap<K, ExpiringValue<V>> map, K key, V value, long ttlSec) {
        map.put(key, ExpiringValue.of(value, ttlSec, TimeUnit.SECONDS));
    }

    public @Nullable Island getIsland(UUID islandId) {
        return getIfValid(islandById, islandId);
    }

    public void putIsland(Island island) {
        put(islandById, island.getId(), island, TTL_ISLAND_SEC);
    }

    public @Nullable UUID getIslandIdByPlayer(UUID playerId) {
        return getIfValid(islandIdByPlayer, playerId);
    }

    public void putIslandIdByPlayer(UUID playerId, UUID islandId) {
        put(islandIdByPlayer, playerId, islandId, TTL_PLAYER_LINK_SEC);
    }

    public @Nullable Players getOwner(UUID islandId) {
        return getIfValid(ownerByIsland, islandId);
    }

    public void putOwner(UUID islandId, Players owner) {
        put(ownerByIsland, islandId, owner, TTL_MEMBERS_SEC);
        putRole(islandId, owner.getMojangId(), RoleType.OWNER);
    }

    public @Nullable List<Players> getMembers(UUID islandId) {
        return getIfValid(membersByIsland, islandId);
    }

    public void putMembers(UUID islandId, List<Players> members) {
        List<Players> copy = List.copyOf(members);
        put(membersByIsland, islandId, copy, TTL_MEMBERS_SEC);
        for (Players p : copy) {
            putRole(islandId, p.getMojangId(), p.getRoleType());
            if (p.getLastKnowName() != null) {
                putRoleByName(islandId, p.getLastKnowName(), p.getRoleType());
            }
        }
    }

    public @Nullable List<Players> getBanned(UUID islandId) {
        return getIfValid(bannedByIsland, islandId);
    }

    public void putBanned(UUID islandId, List<Players> banned) {
        List<Players> copy = List.copyOf(banned);
        put(bannedByIsland, islandId, copy, TTL_MEMBERS_SEC);
        for (Players p : copy) {
            putRole(islandId, p.getMojangId(), RoleType.BAN);
        }
    }

    public @Nullable RoleType getRole(UUID islandId, UUID playerId) {
        return getIfValid(roleByMember, new MemberKey(islandId, playerId));
    }

    public void putRole(UUID islandId, UUID playerId, RoleType role) {
        put(roleByMember, new MemberKey(islandId, playerId), role, TTL_ROLE_SEC);
    }

    public @Nullable RoleType getRoleByName(UUID islandId, String nameLower) {
        return getIfValid(roleByMemberName, new MemberNameKey(islandId, nameLower));
    }

    public void putRoleByName(UUID islandId, String nameLower, RoleType role) {
        put(roleByMemberName, new MemberNameKey(islandId, nameLower), role, TTL_NAME_ROLE_SEC);
    }

    public @Nullable List<WarpIsland> getWarps(UUID islandId) {
        return getIfValid(warpsByIsland, islandId);
    }

    public void putWarps(UUID islandId, List<WarpIsland> warps) {
        put(warpsByIsland, islandId, List.copyOf(warps), TTL_WARPS_SEC);
        for (WarpIsland w : warps) {
            warpByKey.put(new WarpKey(islandId, w.warpName().toLowerCase(Locale.ROOT)), ExpiringValue.of(w, TTL_WARPS_SEC, TimeUnit.SECONDS));
        }
    }

    public @Nullable WarpIsland getWarp(UUID islandId, String name) {
        return getIfValid(warpByKey, new WarpKey(islandId, name.toLowerCase(Locale.ROOT)));
    }

    public void putWarp(UUID islandId, String name, WarpIsland warp) {
        warpByKey.put(new WarpKey(islandId, name.toLowerCase(Locale.ROOT)), ExpiringValue.of(warp, TTL_WARPS_SEC, TimeUnit.SECONDS));
    }

    public @Nullable IslandStateSnapshot getState(UUID islandId) {
        return getIfValid(stateByIsland, islandId);
    }

    public void putState(UUID islandId, IslandStateSnapshot state) {
        put(stateByIsland, islandId, state, TTL_STATE_SEC);
    }

    public void invalidateIsland(UUID islandId) {
        islandById.remove(islandId);
        stateByIsland.remove(islandId);
        ownerByIsland.remove(islandId);
        membersByIsland.remove(islandId);
        bannedByIsland.remove(islandId);
        warpsByIsland.remove(islandId);

        warpByKey.keySet().removeIf(k -> k.islandId.equals(islandId));
    }

    public void invalidateMembers(UUID islandId) {
        ownerByIsland.remove(islandId);
        membersByIsland.remove(islandId);
        bannedByIsland.remove(islandId);
        roleByMember.keySet().removeIf(k -> k.islandId.equals(islandId));
        roleByMemberName.keySet().removeIf(k -> k.islandId().equals(islandId));
    }

    public void invalidateWarps(UUID islandId) {
        warpsByIsland.remove(islandId);
        warpByKey.keySet().removeIf(k -> k.islandId.equals(islandId));
    }

    public void invalidatePlayerLink(UUID playerId) {
        islandIdByPlayer.remove(playerId);
    }

    public void invalidateState(UUID islandId) {
        stateByIsland.remove(islandId);
    }

    private record MemberKey(UUID islandId, UUID playerId) {}

    private record MemberNameKey(UUID islandId, String nameLower) {}

    private record WarpKey(UUID islandId, String nameLower) {
    }

    public record IslandStateSnapshot(boolean disabled, boolean priv, boolean locked, int maxMembers, double size) {
    }
}
