package fr.euphyllia.skyllia.database.sqlite.exec;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.database.IslandMemberQuery;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.enums.RemovalCause;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLiteDatabaseLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class SQLiteIslandMember extends IslandMemberQuery {

    private static final Logger logger = LogManager.getLogger(SQLiteIslandMember.class);

    private static final String UPSERT_MEMBERS = """
            INSERT INTO members_in_islands (island_id, uuid_player, player_name, role, joined)
            VALUES (?, ?, ?, ?, DATETIME('now'))
            ON CONFLICT(island_id, uuid_player)
            DO UPDATE SET role = excluded.role, player_name = excluded.player_name;
            """;

    private static final String DELETE_MEMBERS = """
            DELETE FROM members_in_islands
            WHERE island_id = ? AND uuid_player = ?;
            """;

    private static final String SELECT_MEMBER_ISLAND_MOJANG_ID = """
            SELECT island_id, uuid_player, player_name, role, joined
            FROM members_in_islands
            WHERE island_id = ? AND uuid_player = ?;
            """;

    private static final String SELECT_MEMBER_ISLAND_MOJANG_NAME = """
            SELECT island_id, uuid_player, player_name, role, joined
            FROM members_in_islands
            WHERE island_id = ? AND player_name = ?;
            """;

    private static final String MEMBERS_ISLAND = """
            SELECT island_id, uuid_player, player_name, role, joined
            FROM members_in_islands
            WHERE island_id = ? AND role NOT IN ('BAN', 'VISITOR');
            """;

    private static final String OWNER_ISLAND = """
            SELECT mi.island_id, mi.uuid_player, mi.player_name, mi.role, mi.joined
            FROM members_in_islands mi
            JOIN islands i ON mi.island_id = i.island_id
            WHERE mi.island_id = ?
              AND mi.role = 'OWNER'
              AND i.disable = 0
            LIMIT 1;
            """;

    private static final String ADD_MEMBER_CLEAR = """
            INSERT INTO player_clear (uuid_player, cause)
            VALUES (?, ?)
            ON CONFLICT(uuid_player, cause)
            DO NOTHING;
            """;
    private static final String SELECT_MEMBER_CLEAR = """
            SELECT uuid_player
            FROM player_clear
            WHERE uuid_player = ? AND cause = ?;
            """;
    private static final String DELETE_MEMBER_CLEAR = """
            DELETE FROM player_clear
            WHERE uuid_player = ? AND cause = ?;
            """;

    private final SQLiteDatabaseLoader databaseLoader;
    private final InterneAPI api;

    public SQLiteIslandMember(InterneAPI api, SQLiteDatabaseLoader databaseLoader) {
        this.api = api;
        this.databaseLoader = databaseLoader;
    }

    @Override
    public CompletableFuture<Boolean> updateMember(Island island, Players players) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            databaseLoader.executeUpdate(
                    UPSERT_MEMBERS,
                    List.of(
                            island.getId().toString(),
                            players.getMojangId().toString(),
                            players.getLastKnowName(),
                            players.getRoleType().name()
                    ),
                    affectedRows -> future.complete(affectedRows > 0),
                    null
            );
        } catch (DatabaseException e) {
            future.complete(false);
        }
        return future;
    }

    @Override
    public CompletableFuture<Players> getPlayersIsland(Island island, UUID playerId) {
        CompletableFuture<Players> future = new CompletableFuture<>();
        try {
            databaseLoader.executeQuery(
                    SELECT_MEMBER_ISLAND_MOJANG_ID,
                    List.of(island.getId().toString(), playerId.toString()),
                    rs -> {
                        try {
                            if (rs.next()) {
                                String playerName = rs.getString("player_name");
                                String role = rs.getString("role");
                                Players p = new Players(
                                        playerId,
                                        playerName,
                                        island.getId(),
                                        RoleType.valueOf(role)
                                );
                                future.complete(p);
                            } else {
                                future.complete(null);
                            }
                        } catch (SQLException ex) {
                            logger.error("getPlayersIsland", ex);
                            future.complete(null);
                        }
                    },
                    null
            );
        } catch (DatabaseException e) {
            future.complete(null);
        }
        return future;
    }

    @Override
    public CompletableFuture<@Nullable Players> getPlayersIsland(Island island, String playerName) {
        CompletableFuture<Players> future = new CompletableFuture<>();
        try {
            databaseLoader.executeQuery(
                    SELECT_MEMBER_ISLAND_MOJANG_NAME,
                    List.of(island.getId().toString(), playerName),
                    rs -> {
                        try {
                            if (rs.next()) {
                                UUID uuid = UUID.fromString(rs.getString("uuid_player"));
                                String role = rs.getString("role");
                                Players p = new Players(
                                        uuid,
                                        playerName,
                                        island.getId(),
                                        RoleType.valueOf(role)
                                );
                                future.complete(p);
                            } else {
                                future.complete(null);
                            }
                        } catch (SQLException ex) {
                            logger.error("getPlayersIsland by name", ex);
                            future.complete(null);
                        }
                    },
                    null
            );
        } catch (DatabaseException e) {
            future.complete(null);
        }
        return future;
    }

    @Override
    public CompletableFuture<@Nullable CopyOnWriteArrayList<Players>> getMembersInIsland(Island island) {
        CompletableFuture<CopyOnWriteArrayList<Players>> future = new CompletableFuture<>();
        CopyOnWriteArrayList<Players> playersList = new CopyOnWriteArrayList<>();
        try {
            databaseLoader.executeQuery(
                    MEMBERS_ISLAND,
                    List.of(island.getId().toString()),
                    rs -> {
                        try {
                            boolean any = false;
                            while (rs.next()) {
                                any = true;
                                UUID uuid = UUID.fromString(rs.getString("uuid_player"));
                                RoleType roleType = RoleType.valueOf(rs.getString("role"));
                                String name = rs.getString("player_name");
                                Players p = new Players(uuid, name, island.getId(), roleType);
                                playersList.add(p);
                            }
                            if (!any) {
                                future.complete(null);
                            } else {
                                future.complete(playersList);
                            }
                        } catch (Exception ex) {
                            logger.error("getMembersInIsland", ex);
                            future.complete(null);
                        }
                    },
                    null
            );
        } catch (DatabaseException e) {
            future.complete(null);
        }
        return future;
    }

    @Override
    public CompletableFuture<@Nullable Players> getOwnerInIslandId(Island island) {
        CompletableFuture<Players> future = new CompletableFuture<>();
        try {
            databaseLoader.executeQuery(
                    OWNER_ISLAND,
                    List.of(island.getId().toString()),
                    rs -> {
                        try {
                            if (rs.next()) {
                                String ownerId = rs.getString("uuid_player");
                                String name = rs.getString("player_name");
                                Players p = new Players(
                                        UUID.fromString(ownerId),
                                        name,
                                        island.getId(),
                                        RoleType.OWNER
                                );
                                future.complete(p);
                            } else {
                                future.complete(null);
                            }
                        } catch (SQLException ex) {
                            logger.error("getOwnerInIslandId", ex);
                            future.complete(null);
                        }
                    },
                    null
            );
        } catch (DatabaseException e) {
            future.complete(null);
        }
        return future;
    }

    @Override
    public CompletableFuture<Boolean> addMemberClear(UUID playerId, RemovalCause cause) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            databaseLoader.executeUpdate(
                    ADD_MEMBER_CLEAR,
                    List.of(playerId.toString(), cause.name()),
                    affected -> future.complete(affected > 0),
                    null
            );
        } catch (DatabaseException e) {
            future.complete(false);
        }
        return future;
    }

    @Override
    public CompletableFuture<Boolean> deleteMemberClear(UUID playerId, RemovalCause cause) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            databaseLoader.executeUpdate(
                    DELETE_MEMBER_CLEAR,
                    List.of(playerId.toString(), cause.name()),
                    affected -> future.complete(affected > 0),
                    null
            );
        } catch (DatabaseException e) {
            future.complete(false);
        }
        return future;
    }

    @Override
    public CompletableFuture<Boolean> checkClearMemberExist(UUID playerId, RemovalCause cause) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            databaseLoader.executeQuery(
                    SELECT_MEMBER_CLEAR,
                    List.of(playerId.toString(), cause.name()),
                    rs -> {
                        try {
                            future.complete(rs.next());
                        } catch (SQLException ex) {
                            logger.error("checkClearMemberExist", ex);
                            future.complete(false);
                        }
                    },
                    null
            );
        } catch (DatabaseException e) {
            future.complete(false);
        }
        return future;
    }

    @Override
    public CompletableFuture<Boolean> deleteMember(Island island, Players oldMember) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            databaseLoader.executeUpdate(
                    DELETE_MEMBERS,
                    List.of(island.getId().toString(), oldMember.getMojangId().toString()),
                    affected -> future.complete(affected > 0),
                    null
            );
        } catch (DatabaseException e) {
            future.complete(false);
        }
        return future;
    }
}
