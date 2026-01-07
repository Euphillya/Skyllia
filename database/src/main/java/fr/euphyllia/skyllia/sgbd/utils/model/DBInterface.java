package fr.euphyllia.skyllia.sgbd.utils.model;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;

/**
 * The {@code DBInterface} interface defines a method for retrieving
 * a {@link Connection} to the database. Implementations should handle
 * connection details, such as pooling or direct connections.
 */
public interface DBInterface {

    /**
     * Retrieves a valid {@link Connection} to the database.
     *
     * @return a {@link Connection}, or {@code null} if no connection could be established
     * @throws DatabaseException if an error occurs while obtaining the connection
     */
    @Nullable
    Connection getConnection() throws DatabaseException;
}
