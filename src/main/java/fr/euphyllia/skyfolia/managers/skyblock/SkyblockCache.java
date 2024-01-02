package fr.euphyllia.skyfolia.managers.skyblock;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.Players;
import fr.euphyllia.skyfolia.api.skyblock.model.PermissionsIsland;
import fr.euphyllia.skyfolia.api.skyblock.model.Position;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SkyblockCache {

    private SkyblockManager skyblockManager;
    private final Logger logger = LogManager.getLogger(SkyblockCache.class);

    private final ConcurrentHashMap<UUID, Island> islandId_Island;
    private final ConcurrentHashMap<Position, UUID> position_islandId;
    private final ConcurrentHashMap<UUID, UUID> playerId_IslandId;


    public SkyblockCache(SkyblockManager sm) {
        this.skyblockManager = sm;
        this.islandId_Island = new ConcurrentHashMap<>();
        this.position_islandId = new ConcurrentHashMap<>();
        this.playerId_IslandId = new ConcurrentHashMap<>();
    }

    public void updateCache() {
        for (Player bPlayer : Bukkit.getOnlinePlayers()) {
            Island pIsland = this.skyblockManager.getIslandByOwner(bPlayer.getUniqueId()).join();
            if (pIsland == null) {
                continue;
            }
            this.islandId_Island.put(pIsland.getId(), pIsland);
            this.position_islandId.put(pIsland.getPosition(), pIsland.getId());
            for (Players players : pIsland.getMembers()) {
                this.playerId_IslandId.put(players.getMojangId(), pIsland.getId());
            }
            logger.log(Level.INFO, pIsland.getId() + " est mis en cache");
        }
    }

}
