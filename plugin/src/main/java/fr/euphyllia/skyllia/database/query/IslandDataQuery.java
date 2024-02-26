package fr.euphyllia.skyllia.database.query;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class IslandDataQuery {

    public abstract CompletableFuture<@Nullable Island> getIslandByOwnerId(UUID playerId);

    public abstract CompletableFuture<@Nullable Island> getIslandByPlayerId(UUID playerId);

    public abstract CompletableFuture<Boolean> insertIslands(Island futurIsland);

    public abstract CompletableFuture<@Nullable Island> getIslandByIslandId(UUID islandId);

    public abstract CompletableFuture<Integer> getMaxMemberInIsland(Island island);


}
