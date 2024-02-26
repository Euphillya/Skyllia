package fr.euphyllia.skyllia.database.sqlite.exec;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.database.query.IslandDataQuery;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SqliteIslandData extends IslandDataQuery {
    @Override
    public CompletableFuture<@Nullable Island> getIslandByOwnerId(UUID playerId) {
        return null;
    }

    @Override
    public CompletableFuture<@Nullable Island> getIslandByPlayerId(UUID playerId) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> insertIslands(Island futurIsland) {
        return null;
    }

    @Override
    public CompletableFuture<@Nullable Island> getIslandByIslandId(UUID islandId) {
        return null;
    }

    @Override
    public CompletableFuture<Integer> getMaxMemberInIsland(Island island) {
        return null;
    }
}
