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

    private static final String ADD_MEMBERS = """
                INSERT INTO `%s`.`islands_members`
                    (`island_id`, `uuid_player`, `role`, `joined`)
                    VALUES(?, ?, ?, current_timestamp());
            """;

    private static final String MEMBER_ISLAND_ROLE = """
                SELECT `role`, `joined`
                FROM`%s`.`islands_members`
                WHERE `island_id` = ? AND `uuid_player` = ?;
            """;



    private final Logger logger = LogManager.getLogger(IslandMemberQuery.class);
    private final InterneAPI api;
    private final String databaseName;

    public IslandMemberQuery(InterneAPI api, String databaseName) {
        this.api = api;
        this.databaseName = databaseName;
    }

    public CompletableFuture<Boolean> setRoleTypeMemberInIsland(Island island, UUID playerId, RoleType roleType) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQueryDML(this.api, ADD_MEMBERS.formatted(this.databaseName),
                List.of(island.getIslandId(), playerId, roleType.name()),
                i -> completableFuture.complete(i != 0), null);
        return completableFuture;
    }

    public CompletableFuture<RoleType> getRoleTypeMemberInIsland(Island island, UUID playerId) {
        CompletableFuture<RoleType> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQuery(this.api, MEMBER_ISLAND_ROLE.formatted(this.databaseName),
                List.of(island.getIslandId(), playerId),
                resultSet -> {
                    try {
                        if (resultSet.next()) {
                            String role = resultSet.getString("role");
                            completableFuture.complete(RoleType.valueOf(role));
                        } else {
                            completableFuture.complete(RoleType.VISITOR);
                        }
                    } catch (SQLException e) {
                        completableFuture.complete(RoleType.VISITOR);
                    }
                }, null);
        return completableFuture;
    }

    private static final String MEMBERS_ISLAND = """
                SELECT `island_id`, `uuid_player`, `role`, `joined`
                FROM`%s`.`islands_members`
                WHERE `island_id` = ? AND `role` NOT IN ('BAN', 'VISITOR');
            """;
    public CompletableFuture<@Nullable CopyOnWriteArrayList<Players>> getMembersInIsland(Island island) {
        CompletableFuture<CopyOnWriteArrayList<Players>> completableFuture = new CompletableFuture<>();
        CopyOnWriteArrayList<Players> playersList = new CopyOnWriteArrayList<>();
        MariaDBExecute.executeQuery(this.api, MEMBERS_ISLAND.formatted(this.databaseName),
                List.of(island.getIslandId()),
                resultSet -> {
                    try {
                        if (resultSet.next()) {
                            do {
                                UUID playerId = UUID.fromString(resultSet.getString("uuid_player"));
                                RoleType roleType = RoleType.valueOf(resultSet.getString("role"));
                                Players players = new Players(playerId, Bukkit.getOfflinePlayer(playerId).getName(), island.getIslandId(), roleType);
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
