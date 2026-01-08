package fr.euphyllia.skyllia.database.postgresql;

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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PostgreSQLIslandMember extends IslandMemberQuery {

    private static final String UPSERT_MEMBERS = """
            INSERT INTO members_in_islands (island_id, uuid_player, player_name, role, joined)
            VALUES (?, ?, ?, ?, NOW())
            ON CONFLICT (island_id, uuid_player)
            DO UPDATE SET
                role = EXCLUDED.role,
                player_name = EXCLUDED.player_name;
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

    private static final String OWNERS_ISLAND = """
            SELECT island_id, uuid_player, player_name, role, joined
            FROM members_in_islands
            WHERE island_id = ? AND role = 'OWNER'
            LIMIT 1;
            """;

    private static final String MEMBERS_ISLAND = """
            SELECT island_id, uuid_player, player_name, role, joined
            FROM members_in_islands
            WHERE island_id = ? AND role NOT IN ('BAN', 'VISITOR');
            """;

    private static final String BANNED_MEMBERS_ISLAND = """
            SELECT island_id, uuid_player, player_name, role, joined
            FROM members_in_islands
            WHERE island_id = ? AND role = 'BAN';
            """;

    private static final String OWNER_ISLAND = """
            SELECT mi.island_id, mi.uuid_player, mi.player_name, mi.role, mi.joined
            FROM members_in_islands mi
            JOIN islands i ON mi.island_id = i.island_id
            WHERE mi.island_id = ?
              AND mi.role = 'OWNER'
              AND i.disable = FALSE
            LIMIT 1;
            """;

    private static final String ADD_MEMBER_CLEAR = """
            INSERT INTO player_clear (uuid_player, cause)
            VALUES (?, ?)
            ON CONFLICT (uuid_player, cause) DO NOTHING;
            """;

    private static final String SELECT_MEMBER_CLEAR = """
            SELECT uuid_player
            FROM player_clear
            WHERE uuid_player = ? AND cause = ?
            LIMIT 1;
            """;

    private static final String DELETE_MEMBER_CLEAR = """
            DELETE FROM player_clear
            WHERE uuid_player = ? AND cause = ?;
            """;

    private static final Logger log = LoggerFactory.getLogger(PostgreSQLIslandMember.class);

    private final DatabaseLoader databaseLoader;

    public PostgreSQLIslandMember(DatabaseLoader databaseLoader) {
        this.databaseLoader = databaseLoader;
    }

    @Override
    public @Nullable Players getOwnerByIslandId(UUID islandId) {
        return SQLExecute.queryMap(databaseLoader, OWNERS_ISLAND, List.of(islandId), rs -> {
            try {
                if (rs.next()) {
                    UUID playerId = (UUID) rs.getObject("uuid_player");
                    String playerName = rs.getString("player_name");
                    return new Players(playerId, playerName, islandId, RoleType.OWNER);
                }
            } catch (Exception e) {
                log.error("Error fetching owner by island ID {}", islandId, e);
            }
            return null;
        });
    }

    @Override
    public Boolean updateMember(Island island, Players players) {
        int affected = SQLExecute.update(databaseLoader, UPSERT_MEMBERS, List.of(
                island.getId(),
                players.getMojangId(),
                players.getLastKnowName(),
                players.getRoleType().name()
        ));
        return affected != 0;
    }

    @Override
    public @Nullable Players getPlayersIsland(Island island, UUID playerId) {
        return SQLExecute.queryMap(databaseLoader, SELECT_MEMBER_ISLAND_MOJANG_ID, List.of(
                island.getId(), playerId
        ), rs -> {
            try {
                if (rs.next()) {
                    UUID uuid = (UUID) rs.getObject("uuid_player");
                    String playerName = rs.getString("player_name");
                    RoleType roleType = RoleType.valueOf(rs.getString("role"));
                    return new Players(uuid, playerName, island.getId(), roleType);
                }
            } catch (Exception e) {
                log.error("Error fetching player by island {} and playerId {}", island.getId(), playerId, e);
            }
            return null;
        });
    }

    @Override
    public @Nullable Players getPlayersIsland(Island island, String playerName) {
        return SQLExecute.queryMap(databaseLoader, SELECT_MEMBER_ISLAND_MOJANG_NAME, List.of(
                island.getId(), playerName
        ), rs -> {
            try {
                if (rs.next()) {
                    UUID uuid = (UUID) rs.getObject("uuid_player");
                    RoleType roleType = RoleType.valueOf(rs.getString("role"));
                    String pName = rs.getString("player_name");
                    return new Players(uuid, pName, island.getId(), roleType);
                }
            } catch (Exception e) {
                log.error("Error fetching player by island {} and name {}", island.getId(), playerName, e);
            }
            return null;
        });
    }

    @Override
    public @Nullable List<Players> getMembersInIsland(Island island) {
        List<Players> out = SQLExecute.queryMap(databaseLoader, MEMBERS_ISLAND, List.of(island.getId()), rs -> {
            List<Players> players = new ArrayList<>();
            try {
                while (rs.next()) {
                    UUID playerId = (UUID) rs.getObject("uuid_player");
                    String playerName = rs.getString("player_name");
                    RoleType roleType = RoleType.valueOf(rs.getString("role"));
                    players.add(new Players(playerId, playerName, island.getId(), roleType));
                }
            } catch (Exception e) {
                log.error("Error fetching members in island {}", island.getId(), e);
            }
            return players;
        });
        return out != null ? out : List.of();
    }

    @Override
    public @Nullable List<Players> getBannedMembersInIsland(Island island) {
        List<Players> out = SQLExecute.queryMap(databaseLoader, BANNED_MEMBERS_ISLAND, List.of(island.getId()), rs -> {
            List<Players> players = new ArrayList<>();
            try {
                while (rs.next()) {
                    UUID playerId = (UUID) rs.getObject("uuid_player");
                    String playerName = rs.getString("player_name");
                    RoleType roleType = RoleType.valueOf(rs.getString("role"));
                    players.add(new Players(playerId, playerName, island.getId(), roleType));
                }
            } catch (Exception e) {
                log.error("Error fetching banned members in island {}", island.getId(), e);
            }
            return players;
        });
        return out != null ? out : List.of();
    }

    @Override
    public @Nullable Players getOwnerInIslandId(Island island) {
        return SQLExecute.queryMap(databaseLoader, OWNER_ISLAND, List.of(island.getId()), rs -> {
            try {
                if (rs.next()) {
                    UUID ownerId = (UUID) rs.getObject("uuid_player");
                    String playerName = rs.getString("player_name");
                    return new Players(ownerId, playerName, island.getId(), RoleType.OWNER);
                }
            } catch (Exception e) {
                log.error("Error fetching owner in island {}", island.getId(), e);
            }
            return null;
        });
    }

    @Override
    public Boolean addMemberClear(UUID playerId, RemovalCause cause) {
        int affected = SQLExecute.update(databaseLoader, ADD_MEMBER_CLEAR, List.of(playerId, cause.name()));
        return affected != 0;
    }

    @Override
    public Boolean deleteMemberClear(UUID playerId, RemovalCause cause) {
        int affected = SQLExecute.update(databaseLoader, DELETE_MEMBER_CLEAR, List.of(playerId, cause.name()));
        return affected != 0;
    }

    @Override
    public Boolean checkClearMemberExist(UUID playerId, RemovalCause cause) {
        Boolean exists = SQLExecute.queryMap(databaseLoader, SELECT_MEMBER_CLEAR, List.of(playerId, cause.name()), rs -> {
            try {
                return rs.next();
            } catch (SQLException e) {
                log.error("Error checking clear member existence for {} {}", playerId, cause, e);
                return false;
            }
        });
        return exists != null && exists;
    }

    @Override
    public Boolean deleteMember(Island island, Players oldMember) {
        int affected = SQLExecute.update(databaseLoader, DELETE_MEMBERS, List.of(island.getId(), oldMember.getMojangId()));
        return affected != 0;
    }
}
