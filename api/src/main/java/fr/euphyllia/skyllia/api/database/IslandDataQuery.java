package fr.euphyllia.skyllia.api.database;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
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

    public abstract @Nullable Island getIslandByPosition(Position position);
}
