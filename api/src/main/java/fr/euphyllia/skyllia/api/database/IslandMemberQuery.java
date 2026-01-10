package fr.euphyllia.skyllia.api.database;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.enums.RemovalCause;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * The {@code IslandMemberQuery} class defines an abstract set of methods
 * for managing island members in a SkyBlock context.
 * <p>
 * Implementations should handle operations such as adding, updating,
 * and removing members, as well as retrieving specific members or owners.
 */
public abstract class IslandMemberQuery {

    public abstract Players getOwnerByIslandId(UUID islandId);

    public abstract Boolean updateMember(Island island, Players players);

    public abstract Players getPlayersIsland(Island island, UUID playerId);

    public abstract @Nullable Players getPlayersIsland(Island island, String playerName);

    public abstract @Nullable List<Players> getMembersInIsland(Island island);

    public abstract @Nullable List<Players> getBannedMembersInIsland(Island island);

    public abstract @Nullable Players getOwnerInIslandId(Island island);

    public abstract Boolean addMemberClear(UUID playerId, RemovalCause cause);


    public abstract Boolean deleteMemberClear(UUID playerId, RemovalCause cause);

    public abstract Boolean checkClearMemberExist(UUID playerId, RemovalCause cause);

    public abstract Boolean deleteMember(Island island, Players oldMember);
}
