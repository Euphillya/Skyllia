package fr.euphyllia.skyllia.api.database;

import fr.euphyllia.skyllia.api.coordinate.RegionCoordinate;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import org.bukkit.Location;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * The {@code IslandDataQuery} class defines an abstract set of methods
 * for querying and manipulating island data in a SkyBlock context.
 * <p>
 * Implementations should handle operations such as retrieving islands
 * by owner or player, creating new islands, and obtaining island details.
 */
public abstract class IslandDataQuery {


    @Nullable
    public abstract Island getIslandByOwnerId(UUID playerId);


    @Nullable
    public abstract Island getIslandByPlayerId(UUID playerId);


    public abstract Boolean insertIslands(Island futurIsland);

    public abstract @Nullable Island getIslandByIslandId(UUID islandId);

    public abstract List<Island> getAllIslandsValid();


    public abstract Integer getMaxMemberInIsland(Island island);

    /**
     * Retrieves the island at the given region coordinate.
     *
     * @param region The region coordinate.
     * @return The island, or {@code null} if not found.
     */
    public abstract @Nullable Island getIslandByRegion(RegionCoordinate region);

    /**
     * Retrieves the island at the given position.
     *
     * @param position The position.
     * @return The island, or {@code null} if not found.
     * @deprecated Use {@link #getIslandByRegion(RegionCoordinate)} instead.
     */
    @Deprecated(forRemoval = true, since = "3.x")
    @ApiStatus.ScheduledForRemoval(inVersion = "4.x")
    public @Nullable Island getIslandByPosition(Position position) {
        if (position == null) return null;
        return getIslandByRegion(new RegionCoordinate(position.x(), position.z()));
    }

    public abstract boolean upsertCenterLocation(UUID islandId, Location location);

    public abstract List<Location> getCenterLocations(UUID islandId);
}
