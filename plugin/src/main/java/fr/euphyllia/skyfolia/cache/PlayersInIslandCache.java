package fr.euphyllia.skyfolia.cache;

import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.Players;
import fr.euphyllia.skyfolia.api.skyblock.model.RoleType;
import fr.euphyllia.skyfolia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayersInIslandCache {

    private static final Logger logger = LogManager.getLogger(PlayersInIslandCache.class);
    private static final ConcurrentHashMap<UUID, CopyOnWriteArrayList<Players>> listPlayersInIsland = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, UUID> islandIdByPlayerId = new ConcurrentHashMap<>();

    public static Players getPlayers(UUID islandId, UUID playerId) {
        List<Players> playersInIsland =  listPlayersInIsland.getOrDefault(islandId, new CopyOnWriteArrayList<>());
        if (playersInIsland.isEmpty()) {
            return new Players(playerId, null, islandId, RoleType.VISITOR);
        }
        for (Players players : playersInIsland) {
            if (players.getMojangId() == playerId) {
                return players;
            }
        }
        return new Players(playerId, null, islandId, RoleType.VISITOR);
    }

    public static ConcurrentMap<UUID, CopyOnWriteArrayList<Players>> getListPlayersInIsland() {
        return listPlayersInIsland;
    }

    public static ConcurrentMap<UUID, UUID> getIslandIdByPlayerId() {
        return islandIdByPlayerId;
    }
}
