package fr.euphyllia.skyfolia.managers.skyblock;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.Players;
import fr.euphyllia.skyfolia.api.skyblock.model.IslandType;
import fr.euphyllia.skyfolia.api.skyblock.model.Position;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SkyblockManager {

    private final Main plugin;
    private final Logger logger = LogManager.getLogger(SkyblockManager.class);
    private final ConcurrentHashMap<UUID, Island> islandsCacheByIslandId;
    private final ConcurrentHashMap<Position, Island> islandsCacheByIslandPosition;
    private final ConcurrentHashMap<UUID, Island> islandByPlayerId;

    public SkyblockManager(Main main) {
        this.plugin = main;
        this.islandsCacheByIslandId = new ConcurrentHashMap<>();
        this.islandsCacheByIslandPosition = new ConcurrentHashMap<>();
        this.islandByPlayerId = new ConcurrentHashMap<>();
    }
    
    public CompletableFuture<@Nullable Island> createIsland(Player player, IslandType islandType) {
        CompletableFuture<Island> completableFuture = new CompletableFuture<>();
        try {
            UUID idIsland = UUID.randomUUID();
            Island futurIsland = new Island(
                    islandType.name(),
                    idIsland,
                    player.getUniqueId(),
                    0,
                    0,
                    null
            );
            boolean create = this.plugin.getInterneAPI().getIslandQuery().insertIslands(futurIsland).join();
            if (create) {
                Island island = this.getIslandByOwner(player).join();
                completableFuture.complete(island);
            }

        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage());
            completableFuture.complete(null);
        }
        return completableFuture;
    }

    public CompletableFuture<Boolean> disableIsland(Island island) {
        island.setDisable(true);
        this.islandsCacheByIslandId.put(island.getIslandId(), island);
        this.islandsCacheByIslandPosition.put(island.getPosition(), island);
        for (Players players : island.getMembers()) {
            this.islandByPlayerId.remove(players.getMojangId());
        }
        return this.plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().updateDisable(island);
    }

    public CompletableFuture<@Nullable Island> getIslandByOwner(Player player) {
        CompletableFuture<Island> completableFuture = new CompletableFuture<>();
        try {
            Island island = this.plugin.getInterneAPI().getIslandQuery().getIslandByOwnerId(player.getUniqueId()).join();
            if (island != null) {
                this.islandsCacheByIslandId.put(island.getIslandId(), island);
                this.islandByPlayerId.put(player.getUniqueId(), island);
                if (island.getPosition() != null) {
                    this.islandsCacheByIslandPosition.put(island.getPosition(), island);
                }
            }
            completableFuture.complete(island);
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage());
            completableFuture.complete(null);
        }
        return completableFuture;
    }

    public ConcurrentMap<Position, Island> getIslandsCacheByIslandPosition() {
        return this.islandsCacheByIslandPosition;
    }

    public ConcurrentMap<UUID, Island> getIslandByPlayerId() {
        return this.islandByPlayerId;
    }

    public ConcurrentMap<UUID, Island> getIslandsCacheByIslandId() {
        return this.islandsCacheByIslandId;
    }
}
