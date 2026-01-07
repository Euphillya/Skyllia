package fr.euphyllia.skyllia.api.database;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;

/**
 * The {@code DatabaseInitializeQuery} class provides an abstract definition
 * of an initialization query for a database.
 * <p>
 * Classes extending this abstract class should implement the {@link #init()} method
 * to perform any necessary setup or configuration tasks, such as creating tables or
 * verifying the database schema.
 */
public abstract class DatabaseInitializeQuery {

    /**
     * Initializes the database schema or performs any required setup operations.
     *
     * @return {@code true} if the initialization is successful, {@code false} otherwise
     * @throws DatabaseException if an error occurs during the database initialization
     */
    public abstract Boolean init() throws DatabaseException;
}
