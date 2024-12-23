package fr.euphyllia.skyllia.sgbd.model;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;

/**
 * The {@code DBConnect} interface provides methods to manage the life cycle
 * of a database connection, including initialization, shutdown, and checking
 * connection status.
 */
public interface DBConnect {

    /**
     * Initializes and loads the database connection.
     *
     * @return {@code true} if the connection is successfully established,
     *         {@code false} otherwise
     * @throws DatabaseException if any error occurs during initialization
     */
    boolean onLoad() throws DatabaseException;

    /**
     * Closes the database connection if it is currently open.
     */
    void onClose();

    /**
     * Checks whether the database connection is active and valid.
     *
     * @return {@code true} if the database is connected, {@code false} otherwise
     */
    boolean isConnected();
}
