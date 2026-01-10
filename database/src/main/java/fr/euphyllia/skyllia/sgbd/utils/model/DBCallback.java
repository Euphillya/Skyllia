package fr.euphyllia.skyllia.sgbd.utils.model;

import java.sql.ResultSet;

/**
 * The {@code DBCallback} interface defines a single method to process
 * the {@link ResultSet} returned by a SQL query.
 * <p>
 * Implementations of this interface allow for custom handling of
 * the results obtained from executing a SQL query.
 */
public interface DBCallback {

    /**
     * Processes the provided {@link ResultSet}.
     *
     * @param resultSet the {@link ResultSet} obtained from a SQL query
     */
    void run(ResultSet resultSet);
}
