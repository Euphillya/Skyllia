package fr.euphyllia.skyfolia.managers.skyblock;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.Players;
import fr.euphyllia.skyfolia.api.skyblock.model.IslandType;
import fr.euphyllia.skyfolia.api.skyblock.model.Position;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
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
    private final ConcurrentHashMap<Position, UUID> islandsIdByIslandPosition;
    private final ConcurrentHashMap<UUID, UUID> islandIdByPlayerId;

    public SkyblockManager(Main main) {
        this.plugin = main;
        this.islandsCacheByIslandId = new ConcurrentHashMap<>();
        this.islandsIdByIslandPosition = new ConcurrentHashMap<>();
        this.islandIdByPlayerId = new ConcurrentHashMap<>();
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
        this.islandsIdByIslandPosition.put(island.getPosition(), island.getIslandId());
        for (Players players : island.getMembers()) {
            this.islandIdByPlayerId.remove(players.getMojangId());
        }
        return this.plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().updateDisable(island);
    }

    public CompletableFuture<@Nullable Island> getIslandByOwner(Player player) {
        CompletableFuture<Island> completableFuture = new CompletableFuture<>();
        try {
            Island island = this.plugin.getInterneAPI().getIslandQuery().getIslandByOwnerId(player.getUniqueId()).join();
            if (island != null) {
                this.islandsCacheByIslandId.put(island.getIslandId(), island);
                this.islandIdByPlayerId.put(player.getUniqueId(), island.getIslandId());
                if (island.getPosition() != null) {
                    this.islandsIdByIslandPosition.put(island.getPosition(), island.getIslandId());
                }
            }
            completableFuture.complete(island);
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage());
            completableFuture.complete(null);
        }
        return completableFuture;
    }

    public CompletableFuture<Boolean> addWarpsIsland(Island island, String name, Location playerLocation) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        boolean update = island.addWarps(name, playerLocation);
        if (update) {
            this.islandsCacheByIslandId.put(island.getIslandId(), island);
            return this.plugin.getInterneAPI().getIslandQuery().getIslandWarpQuery().updateWarp(island, name, playerLocation);
        } else {
            completableFuture.complete(false);
        }
        return completableFuture;
    }

    public CompletableFuture<@Nullable Location> getLocationWarp(Island island, String name) {
        return this.plugin.getInterneAPI().getSkyblockManager().getLocationWarp(island, name);
    }

    public ConcurrentMap<Position, UUID> getIslandsIdCacheByIslandPosition() {
        return this.islandsIdByIslandPosition;
    }

    public ConcurrentMap<UUID, UUID> getIslandIdByPlayerId() {
        return this.islandIdByPlayerId;
    }

    public ConcurrentMap<UUID, Island> getIslandsCacheByIslandId() {
        return this.islandsCacheByIslandId;
    }
}
