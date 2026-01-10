package fr.euphyllia.skyllia.sgbd.utils.model;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;

import java.sql.Connection;

/**
 * The {@code DBWork} interface provides a single method
 * for running custom database logic with a given {@link Connection}.
 * <p>
 * Implementations of this interface allow for executing
 * arbitrary SQL statements or other operations within a transaction.
 */
public interface DBWork {

    /**
     * Executes a custom piece of work using the provided {@link Connection}.
     *
     * @param connection the database connection to use
     * @throws DatabaseException if any SQL-related error occurs during the work
     */
    void run(Connection connection) throws DatabaseException;
}
