package fr.euphyllia.skyllia.database.query;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.enums.RemovalCause;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class IslandMemberQuery {

    public abstract CompletableFuture<Boolean> updateMember(Island island, Players players);

    public abstract CompletableFuture<Players> getPlayersIsland(Island island, UUID playerId);

    public abstract CompletableFuture<@Nullable Players> getPlayersIsland(Island island, String playerName);

    public abstract CompletableFuture<@Nullable CopyOnWriteArrayList<Players>> getMembersInIsland(Island island);

    public abstract CompletableFuture<@Nullable Players> getOwnerInIslandId(Island island);

    public abstract CompletableFuture<Boolean> addMemberClear(UUID playerId, RemovalCause cause);

    public abstract CompletableFuture<Boolean> deleteMemberClear(UUID playerId, RemovalCause cause);

    public abstract CompletableFuture<Boolean> checkClearMemberExist(UUID playerId, RemovalCause cause);

    public abstract CompletableFuture<Boolean> deleteMember(Island island, Players oldMember);
}
