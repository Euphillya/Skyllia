package fr.euphyllia.skyllia.database.query.exec;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.database.execute.MariaDBExecute;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class IslandMemberQuery {

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
    private static final String MEMBERS_ISLAND = """
                SELECT `island_id`, `uuid_player`, `player_name`, `role`, `joined`
                FROM `%s`.`members_in_islands`
                WHERE `island_id` = ? AND `role` NOT IN ('BAN', 'VISITOR');
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
            (`uuid_player`) VALUES (?);
            """;
    private static final String SELECT_MEMBER_CLEAR = """
            SELECT `uuid_player` FROM `%s`.`player_clear`
            WHERE `uuid_player` = ?;
            """;

    private static final String DELETE_MEMBER_CLEAR = """
            DELETE FROM `%s`.`player_clear`
                WHERE `uuid_player` = ?;
            """;
    private final Logger logger = LogManager.getLogger(IslandMemberQuery.class);
    private final InterneAPI api;
    private final String databaseName;

    public IslandMemberQuery(InterneAPI api, String databaseName) {
        this.api = api;
        this.databaseName = databaseName;
    }

    public CompletableFuture<Boolean> updateMember(Island island, Players players) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQueryDML(this.api.getDatabaseLoader(), UPSERT_MEMBERS.formatted(this.databaseName),
                List.of(island.getId(), players.getMojangId(), players.getLastKnowName(), players.getRoleType().name(), players.getRoleType().name()),
                i -> completableFuture.complete(i != 0), null);
        return completableFuture;
    }

    public CompletableFuture<Players> getPlayersIsland(Island island, UUID playerId) {
        CompletableFuture<Players> completableFuture = new CompletableFuture<>();
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
        return completableFuture;
    }


    public CompletableFuture<@Nullable Players> getPlayersIsland(Island island, String playerName) {
        CompletableFuture<Players> completableFuture = new CompletableFuture<>();
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
        return completableFuture;
    }

    public CompletableFuture<@Nullable CopyOnWriteArrayList<Players>> getMembersInIsland(Island island) {
        CompletableFuture<CopyOnWriteArrayList<Players>> completableFuture = new CompletableFuture<>();
        CopyOnWriteArrayList<Players> playersList = new CopyOnWriteArrayList<>();
        MariaDBExecute.executeQuery(this.api.getDatabaseLoader(), MEMBERS_ISLAND.formatted(this.databaseName),
                List.of(island.getId()),
                resultSet -> {
                    try {
                        if (resultSet.next()) {
                            do {
                                UUID playerId = UUID.fromString(resultSet.getString("uuid_player"));
                                RoleType roleType = RoleType.valueOf(resultSet.getString("role"));
                                Players players = new Players(playerId, Bukkit.getOfflinePlayer(playerId).getName(), island.getId(), roleType);
                                playersList.add(players);
                            } while (resultSet.next());
                            completableFuture.complete(playersList);
                        }
                    } catch (Exception e) {
                        completableFuture.complete(null);
                    }
                }, null);
        return completableFuture;
    }

    public CompletableFuture<@Nullable Players> getOwnerInIslandId(Island island) {
        CompletableFuture<Players> completableFuture = new CompletableFuture<>();
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
        return completableFuture;
    }

    public CompletableFuture<Boolean> addMemberClear(UUID playerId) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQueryDML(this.api.getDatabaseLoader(), ADD_MEMBER_CLEAR.formatted(this.databaseName), List.of(playerId), i -> completableFuture.complete(i != 0), null);
        return completableFuture;
    }

    public CompletableFuture<Boolean> deleteMemberClear(UUID playerId) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQueryDML(this.api.getDatabaseLoader(), DELETE_MEMBER_CLEAR.formatted(this.databaseName), List.of(playerId), i -> {
            completableFuture.complete(i != 0);
        }, null);
        return completableFuture;
    }

    public CompletableFuture<Boolean> checkClearMemberExist(UUID playerId) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQuery(this.api.getDatabaseLoader(), SELECT_MEMBER_CLEAR.formatted(this.databaseName), List.of(playerId), resultSet -> {
            try {
                completableFuture.complete(resultSet.next());
            } catch (SQLException e) {
                logger.log(Level.FATAL, e.getMessage(), e);
                completableFuture.complete(false);
            }
        }, null);
        return completableFuture;
    }

    public CompletableFuture<Boolean> deleteMember(Island island, Players oldMember) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQueryDML(this.api.getDatabaseLoader(), DELETE_MEMBERS.formatted(this.databaseName),
                List.of(island.getId(), oldMember.getMojangId()),
                var1 -> completableFuture.complete(var1 != 0),
                null);
        return completableFuture;
    }
}
