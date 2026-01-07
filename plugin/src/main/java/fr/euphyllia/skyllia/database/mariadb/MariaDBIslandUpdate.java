package fr.euphyllia.skyllia.database.mariadb;

import fr.euphyllia.skyllia.api.database.IslandUpdateQuery;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MariaDBIslandUpdate extends IslandUpdateQuery {

    private static final String SELECT_STATUS_ISLAND = """
                SELECT `disable`
                FROM `%s`.islands
                WHERE `island_id` = ?;
            """;
    private static final String UPDATE_DISABLE_ISLAND = """
                UPDATE `%s`.islands
                SET `disable` = ?
                WHERE `island_id` = ?;
            """;

    private static final String UPDATE_MEMBERS_ISLAND = """
                UPDATE `%s`.islands
                SET `max_members` = ?
                WHERE `island_id` = ?;
            """;

    private static final String UPDATE_SIZE_ISLAND = """
                UPDATE `%s`.islands
                SET `size` = ?
                WHERE `island_id` = ?;
            """;

    private static final String SELECT_PRIVATE_ISLAND = """
                SELECT  `private`
                FROM `%s`.islands
                WHERE `island_id` = ?;
            """;
    private static final String UPDATE_PRIVATE_ISLAND = """
                UPDATE `%s`.islands
                SET `private` = ?
                WHERE `island_id` = ?;
            """;

    private static final String SELECT_LOCKED_ISLAND = """
                SELECT `locked`
                FROM `%s`.islands
                WHERE `island_id` = ?;
            """;
    private static final String UPDATE_LOCKED_ISLAND = """
                UPDATE `%s`.islands
                SET `locked` = ?
                WHERE `island_id` = ?;
            """;
    private static final Logger log = LoggerFactory.getLogger(MariaDBIslandUpdate.class);

    public DatabaseLoader databaseLoader;
    public String databaseName;

    public MariaDBIslandUpdate(DatabaseLoader databaseLoader, String databaseName) {
        this.databaseLoader = databaseLoader;
        this.databaseName = databaseName;
    }


    @Override
    public Boolean updateDisable(Island island, boolean disable) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        SQLExecute.executeQueryDML(databaseLoader, UPDATE_DISABLE_ISLAND.formatted(databaseName), List.of(
                disable ? 1 : 0,
                island.getId()
        ), i -> atomicBoolean.set(i != 0), null);
        return atomicBoolean.get();
    }

    @Override
    public Boolean updatePrivate(Island island, boolean privateIsland) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        SQLExecute.executeQueryDML(databaseLoader, UPDATE_PRIVATE_ISLAND.formatted(databaseName), List.of(
                privateIsland ? 1 : 0,
                island.getId()
        ), i -> atomicBoolean.set(i != 0), null);
        return atomicBoolean.get();
    }

    @Override
    public Boolean isDisabledIsland(Island island) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        SQLExecute.executeQuery(databaseLoader, SELECT_STATUS_ISLAND.formatted(databaseName), List.of(
                island.getId()
        ), resultSet -> {
            try {
                if (resultSet.next()) {
                    atomicBoolean.set(resultSet.getBoolean("disable"));
                }
            } catch (Exception e) {
                log.error("SQL Exception while checking if island is disabled {}", island.getId(), e);
            }
        }, null);
        return atomicBoolean.get();
    }

    @Override
    public Boolean isPrivateIsland(Island island) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        SQLExecute.executeQuery(databaseLoader, SELECT_PRIVATE_ISLAND.formatted(databaseName), List.of(
                island.getId()
        ), resultSet -> {
            try {
                if (resultSet.next()) {
                    atomicBoolean.set(resultSet.getBoolean("private"));
                }
            } catch (Exception e) {
                log.error("SQL Exception while checking if island is private {}", island.getId(), e);
            }
        }, null);
        return atomicBoolean.get();
    }

    @Override
    public Boolean setMaxMemberInIsland(Island island, int newValue) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        SQLExecute.executeQueryDML(databaseLoader, UPDATE_MEMBERS_ISLAND.formatted(databaseName), List.of(
                newValue,
                island.getId()
        ), i -> atomicBoolean.set(i != 0), null);
        return atomicBoolean.get();
    }

    @Override
    public Boolean setSizeIsland(Island island, double newValue) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        SQLExecute.executeQueryDML(databaseLoader, UPDATE_SIZE_ISLAND.formatted(databaseName), List.of(
                newValue,
                island.getId()
        ), i -> atomicBoolean.set(i != 0), null);
        return atomicBoolean.get();
    }

    @Override
    public Boolean setLockedIsland(Island island, boolean locked) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        SQLExecute.executeQueryDML(databaseLoader, UPDATE_LOCKED_ISLAND.formatted(databaseName), List.of(
                locked ? 1 : 0,
                island.getId()
        ), i -> atomicBoolean.set(i != 0), null);
        return atomicBoolean.get();
    }

    @Override
    public Boolean isLockedIsland(Island island) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        SQLExecute.executeQuery(databaseLoader, SELECT_LOCKED_ISLAND.formatted(databaseName), List.of(
                island.getId()
        ), resultSet -> {
            try {
                if (resultSet.next()) {
                    atomicBoolean.set(resultSet.getInt("locked") == 1);
                }
            } catch (Exception e) {
                log.error("SQL Exception while checking if island is locked {}", island.getId(), e);
            }
        }, null);
        return atomicBoolean.get();
    }
}
