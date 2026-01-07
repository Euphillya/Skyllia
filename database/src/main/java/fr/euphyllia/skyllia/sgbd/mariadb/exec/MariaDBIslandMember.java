package fr.euphyllia.skyllia.sgbd.mariadb.exec;

import fr.euphyllia.skyllia.api.database.IslandMemberQuery;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.enums.RemovalCause;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
    private static final Logger log = LoggerFactory.getLogger(MariaDBIslandMember.class);

    public DatabaseLoader databaseLoader;
    public String databaseName;

    public MariaDBIslandMember(DatabaseLoader databaseLoader, String databaseName) {
        this.databaseLoader = databaseLoader;
        this.databaseName = databaseName;
    }

    @Override
    public Players getOwnerByIslandId(UUID islandId) {
        AtomicReference<Players> players = new AtomicReference<>();
        SQLExecute.executeQuery(databaseLoader, OWNERS_ISLAND.formatted(this.databaseName), List.of(islandId), resultSet -> {
            try {
                if (resultSet.next()) {
                    UUID playerId = UUID.fromString(resultSet.getString("uuid_player"));
                    String playerName = resultSet.getString("player_name");
                    players.set(new Players(playerId, playerName, islandId, RoleType.OWNER));
                }
            } catch (Exception e) {
                log.error("Error fetching owner by island ID", e);
            }
        }, null);
        return players.get();
    }

    @Override
    public Boolean updateMember(Island island, Players players) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        SQLExecute.executeQueryDML(databaseLoader, UPSERT_MEMBERS.formatted(this.databaseName), List.of(island.getId(), players.getMojangId(), players.getLastKnowName(), players.getRoleType().name(), players.getRoleType().name()),
                var1 -> {
            atomicBoolean.set(var1 != 0);
        }, null);
        return atomicBoolean.get();
    }

    @Override
    public Players getPlayersIsland(Island island, UUID playerId) {
        AtomicReference<Players> players = new AtomicReference<>();
        SQLExecute.executeQuery(databaseLoader, SELECT_MEMBER_ISLAND_MOJANG_ID.formatted(this.databaseName), List.of(island.getId(), playerId), resultSet -> {
            try {
                if (resultSet.next()) {
                    UUID mojangId = UUID.fromString(resultSet.getString("uuid_player"));
                    String playerName = resultSet.getString("player_name");
                    RoleType roleType = RoleType.valueOf(resultSet.getString("role"));
                    players.set(new Players(mojangId, playerName, island.getId(), roleType));
                }
            } catch (Exception e) {
                log.error("Error fetching player by island ID and player ID", e);
            }
        }, null);
        return players.get();
    }

    @Override
    public @Nullable Players getPlayersIsland(Island island, String playerName) {
        AtomicReference<Players> players = new AtomicReference<>();
        SQLExecute.executeQuery(databaseLoader, SELECT_MEMBER_ISLAND_MOJANG_NAME.formatted(this.databaseName), List.of(island.getId(), playerName), resultSet -> {
            try {
                if (resultSet.next()) {
                    UUID mojangId = UUID.fromString(resultSet.getString("uuid_player"));
                    String pName = resultSet.getString("player_name");
                    RoleType roleType = RoleType.valueOf(resultSet.getString("role"));
                    players.set(new Players(mojangId, pName, island.getId(), roleType));
                }
            } catch (Exception e) {
                log.error("Error fetching player by island ID and player name", e);
            }
        }, null);
        return players.get();
    }

    @Override
    public @Nullable List<Players> getMembersInIsland(Island island) {
        List<Players> players = new ArrayList<>();
        SQLExecute.executeQuery(databaseLoader, MEMBERS_ISLAND.formatted(this.databaseName), List.of(island.getId()), resultSet -> {
            try {
                while (resultSet.next()) {
                    UUID playerId = UUID.fromString(resultSet.getString("uuid_player"));
                    String playerName = resultSet.getString("player_name");
                    RoleType roleType = RoleType.valueOf(resultSet.getString("role"));
                    players.add(new Players(playerId, playerName, island.getId(), roleType));
                }
            } catch (Exception e) {
                log.error("Error fetching members in island", e);
            }
        }, null);
        return players;
    }

    @Override
    public @Nullable List<Players> getBannedMembersInIsland(Island island) {
        List<Players> players = new ArrayList<>();
        SQLExecute.executeQuery(databaseLoader, BANNED_MEMBERS_ISLAND.formatted(this.databaseName), List.of(island.getId()), resultSet -> {
            try {
                while (resultSet.next()) {
                    UUID playerId = UUID.fromString(resultSet.getString("uuid_player"));
                    String playerName = resultSet.getString("player_name");
                    RoleType roleType = RoleType.valueOf(resultSet.getString("role"));
                    players.add(new Players(playerId, playerName, island.getId(), roleType));
                }
            } catch (Exception e) {
                log.error("Error fetching banned members in island", e);
            }
        }, null);
        return players;
    }

    @Override
    public @Nullable Players getOwnerInIslandId(Island island) {
        AtomicReference<Players> players = new AtomicReference<>();
        SQLExecute.executeQuery(databaseLoader, OWNER_ISLAND.formatted(this.databaseName, this.databaseName), List.of(island.getId()), resultSet -> {
            try {
                if (resultSet.next()) {
                    String ownerId = resultSet.getString("mi.uuid_player");
                    String playerName = resultSet.getString("mi.player_name");
                    Players p = new Players(UUID.fromString(ownerId), playerName, island.getId(), RoleType.OWNER);
                    players.set(p);
                }
            } catch (Exception e) {
                log.error("Error fetching owner in island ID", e);
            }
        }, null);
        return players.get();
    }

    @Override
    public Boolean addMemberClear(UUID playerId, RemovalCause cause) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        SQLExecute.executeQueryDML(databaseLoader, ADD_MEMBER_CLEAR.formatted(this.databaseName), List.of(playerId, cause.name()), var1 -> {
            atomicBoolean.set(var1 != 0);
        }, null);
        return atomicBoolean.get();
    }

    @Override
    public Boolean deleteMemberClear(UUID playerId, RemovalCause cause) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        SQLExecute.executeQueryDML(databaseLoader, DELETE_MEMBER_CLEAR.formatted(this.databaseName), List.of(playerId, cause.name()), var1 -> {
            atomicBoolean.set(var1 != 0);
        }, null);
        return atomicBoolean.get();
    }

    @Override
    public Boolean checkClearMemberExist(UUID playerId, RemovalCause cause) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        SQLExecute.executeQuery(databaseLoader, SELECT_MEMBER_CLEAR.formatted(this.databaseName), List.of(playerId, cause.name()), resultSet -> {
            try {
                atomicBoolean.set(resultSet.next());
            } catch (Exception e) {
                log.error("Error checking clear member existence", e);
            }
        }, null);
        return atomicBoolean.get();
    }

    @Override
    public Boolean deleteMember(Island island, Players oldMember) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        SQLExecute.executeQueryDML(databaseLoader, DELETE_MEMBERS.formatted(this.databaseName),
                List.of(island.getId(), oldMember.getMojangId()),
                var1 -> atomicBoolean.set(var1 != 0),
                null);
        return atomicBoolean.get();
    }
}
