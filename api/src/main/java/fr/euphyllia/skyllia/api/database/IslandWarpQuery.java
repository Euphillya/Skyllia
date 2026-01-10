package fr.euphyllia.skyllia.api.database;

import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public abstract class IslandWarpQuery {

    public abstract Boolean updateWarp(UUID islandId, String warpName, Location location);

    public abstract @Nullable WarpIsland getWarpByName(UUID islandId, String warpName);

    public abstract @Nullable List<WarpIsland> getListWarp(UUID islandId);

    public abstract Boolean deleteWarp(UUID islandId, String name);
}
