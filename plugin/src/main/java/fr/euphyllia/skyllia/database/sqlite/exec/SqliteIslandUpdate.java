package fr.euphyllia.skyllia.database.sqlite.exec;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.database.query.IslandUpdateQuery;

import java.util.concurrent.CompletableFuture;

public class SqliteIslandUpdate extends IslandUpdateQuery {
    @Override
    public CompletableFuture<Boolean> updateDisable(Island island, boolean disable) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> updatePrivate(Island island, boolean privateIsland) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> isDisabledIsland(Island island) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> isPrivateIsland(Island island) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> setMaxMemberInIsland(Island island, int newValue) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> setSizeIsland(Island island, double newValue) {
        return null;
    }
}
