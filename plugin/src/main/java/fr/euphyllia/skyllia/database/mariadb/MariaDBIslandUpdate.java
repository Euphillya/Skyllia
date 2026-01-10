package fr.euphyllia.skyllia.database.mariadb;

import fr.euphyllia.skyllia.api.database.IslandUpdateQuery;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class MariaDBIslandUpdate extends IslandUpdateQuery {

    private static final String SELECT_STATUS_ISLAND = """
            SELECT disable
            FROM islands
            WHERE island_id = ?;
            """;

    private static final String UPDATE_DISABLE_ISLAND = """
            UPDATE islands
            SET disable = ?
            WHERE island_id = ?;
            """;

    private static final String UPDATE_MEMBERS_ISLAND = """
            UPDATE islands
            SET max_members = ?
            WHERE island_id = ?;
            """;

    private static final String UPDATE_SIZE_ISLAND = """
            UPDATE islands
            SET size = ?
            WHERE island_id = ?;
            """;

    private static final String SELECT_PRIVATE_ISLAND = """
            SELECT private
            FROM islands
            WHERE island_id = ?;
            """;

    private static final String UPDATE_PRIVATE_ISLAND = """
            UPDATE islands
            SET private = ?
            WHERE island_id = ?;
            """;

    private static final String SELECT_LOCKED_ISLAND = """
            SELECT locked
            FROM islands
            WHERE island_id = ?;
            """;

    private static final String UPDATE_LOCKED_ISLAND = """
            UPDATE islands
            SET locked = ?
            WHERE island_id = ?;
            """;

    private static final Logger log = LoggerFactory.getLogger(MariaDBIslandUpdate.class);

    private final DatabaseLoader databaseLoader;

    public MariaDBIslandUpdate(DatabaseLoader databaseLoader) {
        this.databaseLoader = databaseLoader;
    }

    @Override
    public Boolean updateDisable(Island island, boolean disable) {
        int affected = SQLExecute.update(
                databaseLoader,
                UPDATE_DISABLE_ISLAND,
                List.of(disable ? 1 : 0, island.getId().toString())
        );
        return affected != 0;
    }

    @Override
    public Boolean updatePrivate(Island island, boolean privateIsland) {
        int affected = SQLExecute.update(
                databaseLoader,
                UPDATE_PRIVATE_ISLAND,
                List.of(privateIsland ? 1 : 0, island.getId().toString())
        );
        return affected != 0;
    }

    @Override
    public Boolean isDisabledIsland(Island island) {
        Boolean disabled = SQLExecute.queryMap(
                databaseLoader,
                SELECT_STATUS_ISLAND,
                List.of(island.getId().toString()),
                rs -> {
                    try {
                        return rs.next() && rs.getBoolean("disable");
                    } catch (SQLException e) {
                        log.error("SQL Exception while checking if island is disabled {}", island.getId(), e);
                        return false;
                    }
                }
        );
        return disabled != null && disabled;
    }

    @Override
    public Boolean isPrivateIsland(Island island) {
        Boolean priv = SQLExecute.queryMap(
                databaseLoader,
                SELECT_PRIVATE_ISLAND,
                List.of(island.getId().toString()),
                rs -> {
                    try {
                        return rs.next() && rs.getBoolean("private");
                    } catch (SQLException e) {
                        log.error("SQL Exception while checking if island is private {}", island.getId(), e);
                        return false;
                    }
                }
        );
        return priv != null && priv;
    }

    @Override
    public Boolean setMaxMemberInIsland(Island island, int newValue) {
        int affected = SQLExecute.update(
                databaseLoader,
                UPDATE_MEMBERS_ISLAND,
                List.of(newValue, island.getId().toString())
        );
        return affected != 0;
    }

    @Override
    public Boolean setSizeIsland(Island island, double newValue) {
        int affected = SQLExecute.update(
                databaseLoader,
                UPDATE_SIZE_ISLAND,
                List.of(newValue, island.getId().toString())
        );
        return affected != 0;
    }

    @Override
    public Boolean setLockedIsland(Island island, boolean locked) {
        int affected = SQLExecute.update(
                databaseLoader,
                UPDATE_LOCKED_ISLAND,
                List.of(locked ? 1 : 0, island.getId().toString())
        );
        return affected != 0;
    }

    @Override
    public Boolean isLockedIsland(Island island) {
        Boolean locked = SQLExecute.queryMap(
                databaseLoader,
                SELECT_LOCKED_ISLAND,
                List.of(island.getId().toString()),
                rs -> {
                    try {
                        return rs.next() && rs.getInt("locked") == 1;
                    } catch (SQLException e) {
                        log.error("SQL Exception while checking if island is locked {}", island.getId(), e);
                        return false;
                    }
                }
        );
        return locked != null && locked;
    }
}
