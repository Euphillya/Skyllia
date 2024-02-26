package fr.euphyllia.skyllia.database.sqlite.exec;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.database.query.IslandWarpQuery;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class SqliteIslandWarp extends IslandWarpQuery {
    @Override
    public CompletableFuture<Boolean> updateWarp(Island island, String warpName, Location location) {
        return null;
    }

    @Override
    public CompletableFuture<@Nullable WarpIsland> getWarpByName(Island island, String warpName) {
        return null;
    }

    @Override
    public CompletableFuture<@Nullable CopyOnWriteArrayList<WarpIsland>> getListWarp(Island island) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteWarp(Island island, String name) {
        return null;
    }
}
