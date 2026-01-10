package fr.euphyllia.skyllia.sgbd.utils.sql;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility class for executing SQL queries with optional callbacks and custom work.
 */
public final class SQLExecute {

    private static final Logger logger = LogManager.getLogger(SQLExecute.class);

    public static void query(@NotNull DatabaseLoader db, @NotNull String sql, @Nullable List<?> params, @Nullable Consumer<ResultSet> consumer) {
        try (Connection c = requireConnection(db);
             PreparedStatement st = c.prepareStatement(sql)) {

            bindParams(st, params);

            try (ResultSet rs = st.executeQuery()) {
                if (consumer != null) consumer.accept(rs);
            }

        } catch (SQLException | DatabaseException e) {
            logger.error("SQL query failed: {}", sql, e);
        }
    }

    public static <T> @Nullable T queryMap(
            @NotNull DatabaseLoader db,
            @NotNull String sql,
            @Nullable List<?> params,
            @NotNull Function<ResultSet, T> mapper
    ) {
        try (Connection c = requireConnection(db);
             PreparedStatement st = c.prepareStatement(sql)) {

            bindParams(st, params);

            try (ResultSet rs = st.executeQuery()) {
                return mapper.apply(rs);
            }

        } catch (SQLException | DatabaseException e) {
            logger.error("SQL queryMap failed: {}", sql, e);
            return null;
        }
    }

    public static int update(
            @NotNull DatabaseLoader db,
            @NotNull String sql,
            @Nullable List<?> params
    ) {
        try (Connection c = requireConnection(db);
             PreparedStatement st = c.prepareStatement(sql)) {

            bindParams(st, params);
            return st.executeUpdate();

        } catch (SQLException | DatabaseException e) {
            logger.error("SQL update failed: {}", sql, e);
            return 0;
        }
    }

    public static void work(@NotNull DatabaseLoader db, @NotNull SQLWork work) {
        try (Connection c = requireConnection(db)) {
            work.run(c);
        } catch (SQLException | DatabaseException e) {
            logger.error("SQL work failed", e);
        }
    }

    public static void transaction(@NotNull DatabaseLoader db, @NotNull SQLWork work) {
        try (Connection c = requireConnection(db)) {
            boolean oldAutoCommit = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                work.run(c);
                c.commit();
            } catch (Exception e) {
                try {
                    c.rollback();
                } catch (SQLException ex) {
                    e.addSuppressed(ex);
                }
                throw e;
            } finally {
                try {
                    c.setAutoCommit(oldAutoCommit);
                } catch (SQLException ignored) {
                }
            }
        } catch (Exception e) {
            logger.error("SQL transaction failed", e);
        }
    }

    private static Connection requireConnection(DatabaseLoader db) throws DatabaseException {
        Connection c = db.getConnection();
        if (c == null) throw new DatabaseException("Cannot get connection to the database");
        return c;
    }

    private static void bindParams(PreparedStatement st, @Nullable List<?> params) throws SQLException {
        if (params == null || params.isEmpty()) return;
        int idx = 1;
        for (Object p : params) {
            st.setObject(idx++, p);
        }
    }

    @FunctionalInterface
    public interface SQLWork {
        void run(Connection connection) throws SQLException, DatabaseException;
    }


}
