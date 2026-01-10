package fr.euphyllia.skyllia.database.sqlite;

import fr.euphyllia.skyllia.api.database.IslandMemberQuery;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.enums.RemovalCause;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLiteIslandMember extends IslandMemberQuery {

    private static final Logger logger = LogManager.getLogger(SQLiteIslandMember.class);

    private static final String UPSERT_MEMBERS = """
            INSERT INTO members_in_islands (island_id, uuid_player, player_name, role, joined)
            VALUES (?, ?, ?, ?, DATETIME('now'))
            ON CONFLICT(island_id, uuid_player)
            DO UPDATE SET
                role = excluded.role,
                player_name = excluded.player_name;
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

    private static final String MEMBERS_BANNED_ISLAND = """
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
              AND i.disable = 0
            LIMIT 1;
            """;

    private static final String ADD_MEMBER_CLEAR = """
            INSERT INTO player_clear (uuid_player, cause)
            VALUES (?, ?)
            ON CONFLICT(uuid_player, cause) DO NOTHING;
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

    private final DatabaseLoader databaseLoader;

    public SQLiteIslandMember(DatabaseLoader databaseLoader) {
        this.databaseLoader = databaseLoader;
    }

    @Override
    public @Nullable Players getOwnerByIslandId(UUID islandId) {
        return SQLExecute.queryMap(databaseLoader, OWNERS_ISLAND, List.of(islandId.toString()), rs -> {
            try {
                if (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid_player"));
                    String name = rs.getString("player_name");
                    return new Players(uuid, name, islandId, RoleType.OWNER);
                }
            } catch (SQLException ex) {
                logger.error("getOwnerByIslandId", ex);
            }
            return null;
        });
    }

    @Override
    public Boolean updateMember(Island island, Players players) {
        int affected = SQLExecute.update(databaseLoader, UPSERT_MEMBERS, List.of(
                island.getId().toString(),
                players.getMojangId().toString(),
                players.getLastKnowName(),
                players.getRoleType().name()
        ));
        return affected > 0;
    }

    @Override
    public @Nullable Players getPlayersIsland(Island island, UUID playerId) {
        return SQLExecute.queryMap(databaseLoader, SELECT_MEMBER_ISLAND_MOJANG_ID, List.of(
                island.getId().toString(),
                playerId.toString()
        ), rs -> {
            try {
                if (rs.next()) {
                    String playerName = rs.getString("player_name");
                    RoleType roleType = RoleType.valueOf(rs.getString("role"));
                    return new Players(playerId, playerName, island.getId(), roleType);
                }
            } catch (Exception ex) {
                logger.error("getPlayersIsland", ex);
            }
            return null;
        });
    }

    @Override
    public @Nullable Players getPlayersIsland(Island island, String playerName) {
        return SQLExecute.queryMap(databaseLoader, SELECT_MEMBER_ISLAND_MOJANG_NAME, List.of(
                island.getId().toString(),
                playerName
        ), rs -> {
            try {
                if (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid_player"));
                    RoleType roleType = RoleType.valueOf(rs.getString("role"));
                    String pName = rs.getString("player_name");
                    return new Players(uuid, pName, island.getId(), roleType);
                }
            } catch (Exception ex) {
                logger.error("getPlayersIsland by name", ex);
            }
            return null;
        });
    }

    @Override
    public List<Players> getMembersInIsland(Island island) {
        List<Players> out = SQLExecute.queryMap(databaseLoader, MEMBERS_ISLAND, List.of(island.getId().toString()), rs -> {
            List<Players> players = new ArrayList<>();
            try {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid_player"));
                    RoleType roleType = RoleType.valueOf(rs.getString("role"));
                    String name = rs.getString("player_name");
                    players.add(new Players(uuid, name, island.getId(), roleType));
                }
            } catch (Exception ex) {
                logger.error("getMembersInIsland", ex);
            }
            return players;
        });

        return out != null ? out : List.of();
    }

    @Override
    public List<Players> getBannedMembersInIsland(Island island) {
        List<Players> out = SQLExecute.queryMap(databaseLoader, MEMBERS_BANNED_ISLAND, List.of(island.getId().toString()), rs -> {
            List<Players> players = new ArrayList<>();
            try {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid_player"));
                    RoleType roleType = RoleType.valueOf(rs.getString("role"));
                    String name = rs.getString("player_name");
                    players.add(new Players(uuid, name, island.getId(), roleType));
                }
            } catch (Exception ex) {
                logger.error("getBannedMembersInIsland", ex);
            }
            return players;
        });

        return out != null ? out : List.of();
    }

    @Override
    public @Nullable Players getOwnerInIslandId(Island island) {
        return SQLExecute.queryMap(databaseLoader, OWNER_ISLAND, List.of(island.getId().toString()), rs -> {
            try {
                if (rs.next()) {
                    UUID ownerId = UUID.fromString(rs.getString("uuid_player"));
                    String name = rs.getString("player_name");
                    return new Players(ownerId, name, island.getId(), RoleType.OWNER);
                }
            } catch (Exception ex) {
                logger.error("getOwnerInIslandId", ex);
            }
            return null;
        });
    }

    @Override
    public Boolean addMemberClear(UUID playerId, RemovalCause cause) {
        SQLExecute.update(databaseLoader, ADD_MEMBER_CLEAR, List.of(playerId.toString(), cause.name()));
        return checkClearMemberExist(playerId, cause);
    }

    @Override
    public Boolean deleteMemberClear(UUID playerId, RemovalCause cause) {
        int affected = SQLExecute.update(databaseLoader, DELETE_MEMBER_CLEAR, List.of(playerId.toString(), cause.name()));
        return affected > 0;
    }

    @Override
    public Boolean checkClearMemberExist(UUID playerId, RemovalCause cause) {
        Boolean exists = SQLExecute.queryMap(databaseLoader, SELECT_MEMBER_CLEAR, List.of(playerId.toString(), cause.name()), rs -> {
            try {
                return rs.next();
            } catch (SQLException ex) {
                logger.error("checkClearMemberExist", ex);
                return false;
            }
        });
        return exists != null && exists;
    }

    @Override
    public Boolean deleteMember(Island island, Players oldMember) {
        int affected = SQLExecute.update(databaseLoader, DELETE_MEMBERS, List.of(
                island.getId().toString(),
                oldMember.getMojangId().toString()
        ));
        return affected > 0;
    }
}
