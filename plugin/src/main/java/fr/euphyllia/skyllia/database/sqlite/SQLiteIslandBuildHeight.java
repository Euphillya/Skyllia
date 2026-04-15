package fr.euphyllia.skyllia.database.sqlite;

import fr.euphyllia.skyllia.api.database.IslandBuildHeightQuery;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLiteDatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class SQLiteIslandBuildHeight extends IslandBuildHeightQuery {

    private static final Logger log = LoggerFactory.getLogger(SQLiteIslandBuildHeight.class);

    private static final String SELECT_MIN = """
            SELECT min_height
            FROM islands_build_height
            WHERE island_id = ? AND world_name = ?;
            """;

    private static final String SELECT_MAX = """
            SELECT max_height
            FROM islands_build_height
            WHERE island_id = ? AND world_name = ?;
            """;


    private static final String UPSERT_MIN = """
            INSERT INTO islands_build_height (island_id, world_name, min_height, max_height)
            VALUES (?, ?, ?, 0)
            ON CONFLICT(island_id, world_name) DO UPDATE SET min_height = excluded.min_height;
            """;

    private static final String UPSERT_MAX = """
            INSERT INTO islands_build_height (island_id, world_name, min_height, max_height)
            VALUES (?, ?, 0, ?)
            ON CONFLICT(island_id, world_name) DO UPDATE SET max_height = excluded.max_height;
            """;

    private final SQLiteDatabaseLoader databaseLoader;

    public SQLiteIslandBuildHeight(SQLiteDatabaseLoader databaseLoader) {
        this.databaseLoader = databaseLoader;
    }

    @Override
    public @Nullable Integer getMinHeight(Island island, String worldName) {
        return SQLExecute.queryMap(
                databaseLoader,
                SELECT_MIN,
                List.of(island.getId().toString(), worldName),
                rs -> {
                    try {
                        return rs.next() ? rs.getInt("min_height") : null;
                    } catch (SQLException e) {
                        log.error("SQL error reading min_height for island {} world {}", island.getId(), worldName, e);
                        return null;
                    }
                }
        );
    }

    @Override
    public @Nullable Integer getMaxHeight(Island island, String worldName) {
        return SQLExecute.queryMap(
                databaseLoader,
                SELECT_MAX,
                List.of(island.getId().toString(), worldName),
                rs -> {
                    try {
                        return rs.next() ? rs.getInt("max_height") : null;
                    } catch (SQLException e) {
                        log.error("SQL error reading max_height for island {} world {}", island.getId(), worldName, e);
                        return null;
                    }
                }
        );
    }

    @Override
    public boolean setMinHeight(Island island, String worldName, int value) {
        int affected = SQLExecute.update(
                databaseLoader,
                UPSERT_MIN,
                List.of(island.getId().toString(), worldName, value)
        );
        return affected > 0;
    }

    @Override
    public boolean setMaxHeight(Island island, String worldName, int value) {
        int affected = SQLExecute.update(
                databaseLoader,
                UPSERT_MAX,
                List.of(island.getId().toString(), worldName, value)
        );
        return affected > 0;
    }
}
