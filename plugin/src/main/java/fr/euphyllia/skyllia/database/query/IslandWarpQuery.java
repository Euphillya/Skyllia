package fr.euphyllia.skyllia.database.query;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class IslandWarpQuery {

    public abstract CompletableFuture<Boolean> updateWarp(Island island, String warpName, Location location);

    public abstract CompletableFuture<@Nullable WarpIsland> getWarpByName(Island island, String warpName);

    public abstract CompletableFuture<@Nullable CopyOnWriteArrayList<WarpIsland>> getListWarp(Island island);

    public abstract CompletableFuture<Boolean> deleteWarp(Island island, String name);

}
