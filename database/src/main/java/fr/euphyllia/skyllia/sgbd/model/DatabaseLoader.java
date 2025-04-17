package fr.euphyllia.skyllia.sgbd.model;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import org.jetbrains.annotations.Nullable;

public interface DatabaseLoader {

    /**
     * Loads the database connection if not already connected.
     *
     * @return {@code true} if the database was successfully connected; {@code false} otherwise.
     * @throws DatabaseException if an error occurs while initializing the connection pool.
     */
    public abstract boolean loadDatabase() throws DatabaseException;

    /**
     * Closes the database connection if it is currently open.
     */
    public abstract void closeDatabase();

    /**
     * Retrieves a Connection instance.
     *
     * @return a valid Connection or {@code null}.
     * @throws DatabaseException if an error occurs while retrieving the connection.
     */
    public abstract @Nullable Object getConnection() throws DatabaseException;
}
