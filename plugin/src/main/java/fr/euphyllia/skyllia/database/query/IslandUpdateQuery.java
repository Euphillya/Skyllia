package fr.euphyllia.skyllia.database.query;

import fr.euphyllia.skyllia.api.skyblock.Island;

import java.util.concurrent.CompletableFuture;

public abstract class IslandUpdateQuery {

    public abstract CompletableFuture<Boolean> updateDisable(Island island, boolean disable);

    public abstract CompletableFuture<Boolean> updatePrivate(Island island, boolean privateIsland);

    public abstract CompletableFuture<Boolean> isDisabledIsland(Island island);

    public abstract CompletableFuture<Boolean> isPrivateIsland(Island island);

    public abstract CompletableFuture<Boolean> setMaxMemberInIsland(Island island, int newValue);

    public abstract CompletableFuture<Boolean> setSizeIsland(Island island, double newValue);

}
