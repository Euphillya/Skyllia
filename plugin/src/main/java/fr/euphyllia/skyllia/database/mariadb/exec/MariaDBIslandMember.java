package fr.euphyllia.skyllia.database.mariadb.exec;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.database.IslandMemberQuery;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.enums.RemovalCause;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.utils.sql.MariaDBExecute;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class MariaDBIslandMember extends IslandMemberQuery {

    private static final String UPSERT_MEMBERS = """
                INSERT INTO `%s`.`members_in_islands`
                    (`island_id`, `uuid_player`, `player_name`, `role`, `joined`)
                    VALUES(?, ?, ?, ?, current_timestamp())
                    on DUPLICATE key UPDATE `role` = ?;
            """;

    private static final String DELETE_MEMBERS = """
                DELETE FROM `%s`.`members_in_islands`
                    WHERE `island_id`= ? AND `uuid_player`= ?;
            """;

    private static final String SELECT_MEMBER_ISLAND_MOJANG_ID = """
                SELECT `island_id`, `uuid_player`, `player_name`, `role`, `joined`
                FROM `%s`.`members_in_islands`
                WHERE `island_id` = ? AND `uuid_player` = ?;
            """;

    private static final String SELECT_MEMBER_ISLAND_MOJANG_NAME = """
                SELECT `island_id`, `uuid_player`, `player_name`, `role`, `joined`
                FROM `%s`.`members_in_islands`
                WHERE `island_id` = ? AND `player_name` = ?;
            """;

    private static final String OWNERS_ISLAND = """
                SELECT `island_id`, `uuid_player`, `player_name, `role`, `joined`
                FROM `%s`.`members_in_islands`
                WHERE `island_id` = ? AND `role` = 'OWNER';
            """;

    private static final String MEMBERS_ISLAND = """
                SELECT `island_id`, `uuid_player`, `player_name`, `role`, `joined`
                FROM `%s`.`members_in_islands`
                WHERE `island_id` = ? AND `role` NOT IN ('BAN', 'VISITOR');
            """;

    private static final String BANNED_MEMBERS_ISLAND = """
                SELECT `island_id`, `uuid_player`, `player_name`, `role`, `joined`
                FROM `%s`.`members_in_islands`
                WHERE `island_id` = ? AND `role` = 'BAN';
            """;

    private static final String OWNER_ISLAND = """
            SELECT mi.*
            FROM `%s`.`members_in_islands` mi
            JOIN `%s`.`islands` i ON mi.`island_id` = i.`island_id`
            WHERE mi.`island_id` = ?
            AND mi.`role` = "OWNER"
            AND i.disable = 0;
            """;

    private static final String ADD_MEMBER_CLEAR = """
            INSERT INTO `%s`.`player_clear`
            (`uuid_player`, `cause`) VALUES (?, ?);
            """;
    private static final String SELECT_MEMBER_CLEAR = """
            SELECT `uuid_player` FROM `%s`.`player_clear`
            WHERE `uuid_player` = ? AND `cause` = ?;
            """;

    private static final String DELETE_MEMBER_CLEAR = """
            DELETE FROM `%s`.`player_clear`
                WHERE `uuid_player` = ? AND `cause` = ?;
            """;
    private final Logger logger = LogManager.getLogger(MariaDBIslandMember.class);
    private final InterneAPI api;
    private final String databaseName;

    public MariaDBIslandMember(InterneAPI api, String databaseName) {
        this.api = api;
        this.databaseName = databaseName;
    }

    @Override
    public CompletableFuture<@Nullable Players> getOwnerByIslandId(UUID islandId) {
        CompletableFuture<Players> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQuery(this.api.getDatabaseLoader(), OWNERS_ISLAND.formatted(this.databaseName),
                    List.of(islandId),
                    resultSet -> {
                        try {
                            if (resultSet.next()) {
                                UUID playerId = UUID.fromString(resultSet.getString("uuid_player"));
                                String playerName = resultSet.getString("player_name");
                                Players players = new Players(playerId, playerName, islandId, RoleType.OWNER);
                                completableFuture.complete(players);
                            } else {
                                completableFuture.complete(null);
                            }
                        } catch (SQLException e) {
                            completableFuture.complete(null);
                        }
                    }, null);
        } catch (DatabaseException e) {
            completableFuture.complete(null);
        }
        return completableFuture;
    }

    public CompletableFuture<Boolean> updateMember(Island island, Players players) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQueryDML(this.api.getDatabaseLoader(), UPSERT_MEMBERS.formatted(this.databaseName),
                    List.of(island.getId(), players.getMojangId(), players.getLastKnowName(), players.getRoleType().name(), players.getRoleType().name()),
                    i -> completableFuture.complete(i != 0), null);
        } catch (DatabaseException e) {
            completableFuture.complete(false);
        }
        return completableFuture;
    }

    public CompletableFuture<Players> getPlayersIsland(Island island, UUID playerId) {
        CompletableFuture<Players> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQuery(this.api.getDatabaseLoader(), SELECT_MEMBER_ISLAND_MOJANG_ID.formatted(this.databaseName),
                    List.of(island.getId(), playerId),
                    resultSet -> {
                        try {
                            if (resultSet.next()) {
                                String playerName = resultSet.getString("player_name");
                                String role = resultSet.getString("role");
                                Players players = new Players(playerId, playerName, island.getId(), RoleType.valueOf(role));
                                completableFuture.complete(players);
                            } else {
                                completableFuture.complete(null);
                            }
                        } catch (SQLException e) {
                            completableFuture.complete(null);
                        }
                    }, null);
        } catch (DatabaseException e) {
            completableFuture.complete(null);
        }
        return completableFuture;
    }


    public CompletableFuture<@Nullable Players> getPlayersIsland(Island island, String playerName) {
        CompletableFuture<Players> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQuery(this.api.getDatabaseLoader(), SELECT_MEMBER_ISLAND_MOJANG_NAME.formatted(this.databaseName),
                    List.of(island.getId(), playerName),
                    resultSet -> {
                        try {
                            if (resultSet.next()) {
                                UUID playerId = UUID.fromString(resultSet.getString("uuid_player"));
                                String role = resultSet.getString("role");
                                Players players = new Players(playerId, playerName, island.getId(), RoleType.valueOf(role));
                                completableFuture.complete(players);
                            } else {
                                completableFuture.complete(null);
                            }
                        } catch (SQLException e) {
                            completableFuture.complete(null);
                        }
                    }, null);
        } catch (DatabaseException e) {
            completableFuture.complete(null);
        }
        return completableFuture;
    }

    public CompletableFuture<@Nullable CopyOnWriteArrayList<Players>> getMembersInIsland(Island island) {
        CompletableFuture<CopyOnWriteArrayList<Players>> completableFuture = new CompletableFuture<>();
        CopyOnWriteArrayList<Players> playersList = new CopyOnWriteArrayList<>();
        try {
            MariaDBExecute.executeQuery(this.api.getDatabaseLoader(), MEMBERS_ISLAND.formatted(this.databaseName),
                    List.of(island.getId()),
                    resultSet -> {
                        try {
                            if (resultSet.next()) {
                                do {
                                    UUID playerId = UUID.fromString(resultSet.getString("uuid_player"));
                                    RoleType roleType = RoleType.valueOf(resultSet.getString("role"));
                                    String playerName = resultSet.getString("player_name");
                                    Players players = new Players(playerId, playerName, island.getId(), roleType);
                                    playersList.add(players);
                                } while (resultSet.next());
                                completableFuture.complete(playersList);
                            } else {
                                completableFuture.complete(new CopyOnWriteArrayList<>());
                            }
                        } catch (Exception e) {
                            completableFuture.complete(new CopyOnWriteArrayList<>());
                        }
                    }, null);
        } catch (DatabaseException e) {
            completableFuture.complete(new CopyOnWriteArrayList<>());
        }
        return completableFuture;
    }

    @Override
    public CompletableFuture<@Nullable CopyOnWriteArrayList<Players>> getBannedMembersInIsland(Island island) {
        CompletableFuture<CopyOnWriteArrayList<Players>> completableFuture = new CompletableFuture<>();
        CopyOnWriteArrayList<Players> playersList = new CopyOnWriteArrayList<>();
        try {
            MariaDBExecute.executeQuery(this.api.getDatabaseLoader(), BANNED_MEMBERS_ISLAND.formatted(this.databaseName),
                    List.of(island.getId()),
                    resultSet -> {
                        try {
                            if (resultSet.next()) {
                                do {
                                    UUID playerId = UUID.fromString(resultSet.getString("uuid_player"));
                                    RoleType roleType = RoleType.valueOf(resultSet.getString("role"));
                                    String playerName = resultSet.getString("player_name");
                                    Players players = new Players(playerId, playerName, island.getId(), roleType);
                                    playersList.add(players);
                                } while (resultSet.next());
                                completableFuture.complete(playersList);
                            } else {
                                completableFuture.complete(new CopyOnWriteArrayList<>());
                            }
                        } catch (Exception e) {
                            logger.log(Level.FATAL, e.getMessage(), e);
                            completableFuture.complete(new CopyOnWriteArrayList<>());
                        }
                    }, null);
        } catch (Exception e) {
            completableFuture.complete(new CopyOnWriteArrayList<>());
            logger.log(Level.FATAL, e.getMessage(), e);
        }
        return completableFuture;
    }

    public CompletableFuture<@Nullable Players> getOwnerInIslandId(Island island) {
        CompletableFuture<Players> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQuery(this.api.getDatabaseLoader(), OWNER_ISLAND.formatted(this.databaseName, this.databaseName), List.of(island.getId()), resultSet -> {
                try {
                    if (resultSet.next()) {
                        String ownerId = resultSet.getString("mi.uuid_player");
                        String playerName = resultSet.getString("mi.player_name");
                        Players players = new Players(UUID.fromString(ownerId), playerName, island.getId(), RoleType.OWNER);
                        completableFuture.complete(players);
                    }
                    completableFuture.complete(null);
                } catch (SQLException e) {
                    logger.log(Level.FATAL, e.getMessage(), e);
                    completableFuture.complete(null);
                }
            }, null);
        } catch (DatabaseException e) {
            completableFuture.complete(null);
        }
        return completableFuture;
    }

    public CompletableFuture<Boolean> addMemberClear(UUID playerId, RemovalCause cause) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQueryDML(this.api.getDatabaseLoader(), ADD_MEMBER_CLEAR.formatted(this.databaseName), List.of(playerId, cause.name()), i -> completableFuture.complete(i != 0), null);
        } catch (DatabaseException e) {
            completableFuture.complete(false);
        }
        return completableFuture;
    }

    public CompletableFuture<Boolean> deleteMemberClear(UUID playerId, RemovalCause cause) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQueryDML(this.api.getDatabaseLoader(), DELETE_MEMBER_CLEAR.formatted(this.databaseName), List.of(playerId, cause.name()), i -> {
                completableFuture.complete(i != 0);
            }, null);
        } catch (DatabaseException e) {
            completableFuture.complete(false);
        }
        return completableFuture;
    }

    public CompletableFuture<Boolean> checkClearMemberExist(UUID playerId, RemovalCause cause) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQuery(this.api.getDatabaseLoader(), SELECT_MEMBER_CLEAR.formatted(this.databaseName), List.of(playerId, cause.name()), resultSet -> {
                try {
                    completableFuture.complete(resultSet.next());
                } catch (SQLException e) {
                    logger.log(Level.FATAL, e.getMessage(), e);
                    completableFuture.complete(false);
                }
            }, null);
        } catch (DatabaseException e) {
            completableFuture.complete(false);
        }
        return completableFuture;
    }

    public CompletableFuture<Boolean> deleteMember(Island island, Players oldMember) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQueryDML(this.api.getDatabaseLoader(), DELETE_MEMBERS.formatted(this.databaseName),
                    List.of(island.getId(), oldMember.getMojangId()),
                    var1 -> completableFuture.complete(var1 != 0),
                    null);
        } catch (DatabaseException e) {
            completableFuture.complete(false);
        }
        return completableFuture;
    }
}
