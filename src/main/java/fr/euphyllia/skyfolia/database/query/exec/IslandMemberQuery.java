package fr.euphyllia.skyfolia.database.query.exec;

import fr.euphyllia.skyfolia.api.InterneAPI;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.Players;
import fr.euphyllia.skyfolia.api.skyblock.model.RoleType;
import fr.euphyllia.skyfolia.database.execute.MariaDBExecute;
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

    private static final String SELECT_MEMBER_ISLAND_MOJANG_ID = """
                SELECT `island_id`, `uuid_player`, `player_name`, `role`, `joined`
                FROM`%s`.`members_in_islands`
                WHERE `island_id` = ? AND `uuid_player` = ?;
            """;

    private static final String SELECT_MEMBER_ISLAND_MOJANG_NAME = """
                SELECT `island_id`, `uuid_player`, `player_name`, `role`, `joined`
                FROM`%s`.`members_in_islands`
                WHERE `island_id` = ? AND `player_name` = ?;
            """;
    private static final String MEMBERS_ISLAND = """
                SELECT `island_id`, `uuid_player`, `player_name`, `role`, `joined`
                FROM`%s`.`members_in_islands`
                WHERE `island_id` = ? AND `role` NOT IN ('BAN', 'VISITOR');
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
        MariaDBExecute.executeQueryDML(this.api, UPSERT_MEMBERS.formatted(this.databaseName),
                List.of(island.getId(), players.getMojangId(), players.getLastKnowName(), players.getRoleType().name(), players.getRoleType().name()),
                i -> completableFuture.complete(i != 0), null);
        return completableFuture;
    }

    public CompletableFuture<Players> getPlayersIsland(Island island, UUID playerId) {
        CompletableFuture<Players> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQuery(this.api, SELECT_MEMBER_ISLAND_MOJANG_ID.formatted(this.databaseName),
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


    public CompletableFuture<Players> getPlayersIsland(Island island, String playerName) {
        CompletableFuture<Players> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQuery(this.api, SELECT_MEMBER_ISLAND_MOJANG_NAME.formatted(this.databaseName),
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
        MariaDBExecute.executeQuery(this.api, MEMBERS_ISLAND.formatted(this.databaseName),
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
}
