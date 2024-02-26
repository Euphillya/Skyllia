package fr.euphyllia.skyllia.database.sqlite.exec;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.database.query.IslandMemberQuery;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class SqliteIslandMember extends IslandMemberQuery {

    @Override
    public CompletableFuture<Boolean> updateMember(Island island, Players players) {
        return null;
    }

    @Override
    public CompletableFuture<Players> getPlayersIsland(Island island, UUID playerId) {
        return null;
    }

    @Override
    public CompletableFuture<@Nullable Players> getPlayersIsland(Island island, String playerName) {
        return null;
    }

    @Override
    public CompletableFuture<@Nullable CopyOnWriteArrayList<Players>> getMembersInIsland(Island island) {
        return null;
    }

    @Override
    public CompletableFuture<@Nullable Players> getOwnerInIslandId(Island island) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> addMemberClear(UUID playerId) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteMemberClear(UUID playerId) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> checkClearMemberExist(UUID playerId) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteMember(Island island, Players oldMember) {
        return null;
    }
}
