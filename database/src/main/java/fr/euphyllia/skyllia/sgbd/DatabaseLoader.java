package fr.euphyllia.skyllia.sgbd;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.stream.AsciiStream;
import fr.euphyllia.skyllia.sgbd.stream.BinaryStream;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The {@code DatabaseLoader} class provides methods to load and close a MariaDB connection pool,
 * execute SQL queries, and handle prepared statements with various parameter types.
 */
public class DatabaseLoader {

    private final MariaDB mariaDB;

    /**
     * Constructs a new {@code DatabaseLoader} with the specified {@link MariaDB} instance.
     *
     * @param mariaDB the {@link MariaDB} instance used to manage connections.
     */
    public DatabaseLoader(MariaDB mariaDB) {
        this.mariaDB = mariaDB;
    }

    /**
     * Loads the database connection if not already connected.
     *
     * @return {@code true} if the database was successfully connected; {@code false} otherwise.
     * @throws DatabaseException if an error occurs while initializing the connection pool.
     */
    public boolean loadDatabase() throws DatabaseException {
        if (mariaDB != null && !mariaDB.isConnected()) {
            return mariaDB.onLoad();
        }
        return false;
    }

    /**
     * Closes the database connection if it is currently open.
     */
    public void closeDatabase() {
        if (mariaDB != null) {
            mariaDB.onClose();
        }
    }

    /**
     * Retrieves a {@link Connection} from the {@link MariaDB} instance.
     *
     * @return a valid {@link Connection}, or {@code null} if {@code mariaDB} is {@code null}.
     * @throws DatabaseException if an error occurs while retrieving the connection.
     */
    @Nullable
    public Connection getMariaDBConnection() throws DatabaseException {
        return mariaDB != null ? mariaDB.getConnection() : null;
    }

    /**
     * Executes an SQL statement that modifies data (e.g., INSERT, UPDATE, DELETE)
     * and returns the number of affected rows.
     *
     * @param connection the active SQL {@link Connection}.
     * @param query the SQL query string to be executed.
     * @param param a list of parameters (in the correct order) to be bound to the query.
     * @return the number of rows affected by the query.
     * @throws SQLException if an error occurs during the SQL execution.
     */
    public int executeInt(Connection connection, String query, List<?> param) throws SQLException {
        return this.getStatementFinal(connection, query, param).join().executeUpdate();
    }

    /**
     * Creates and prepares a {@link PreparedStatement} asynchronously using the provided query and parameters.
     *
     * @param connection the active SQL {@link Connection}.
     * @param query the SQL query string to prepare.
     * @param param a list of parameters to set on the {@link PreparedStatement}.
     * @return a {@link CompletableFuture} containing the prepared statement.
     * @throws SQLException if an error occurs while preparing the statement.
     */
    private CompletableFuture<PreparedStatement> getStatementFinal(Connection connection, String query, List<?> param)
            throws SQLException {
        CompletableFuture<PreparedStatement> completableFuture = new CompletableFuture<>();
        PreparedStatement statement = connection.prepareStatement(query);
        try {
            if (param != null) {
                int i = 1;
                for (Object value : param) {
                    this.insertStatement(i++, statement, value);
                }
            }
        } finally {
            // Ensure the statement is always completed in the future
            completableFuture.complete(statement);
        }
        return completableFuture;
    }

    /**
     * Inserts a value into the {@link PreparedStatement} at the specified parameter index,
     * supporting multiple data types.
     *
     * @param i the parameter index.
     * @param statement the {@link PreparedStatement} where the value will be inserted.
     * @param value the value to set in the statement parameter.
     * @throws SQLException if an error occurs while setting the parameter value.
     */
    private void insertStatement(int i, PreparedStatement statement, Object value) throws SQLException {
        switch (value) {
            case byte[] valueBytes -> statement.setBytes(i, valueBytes);
            case Timestamp valueTimes -> statement.setTimestamp(i, valueTimes);
            case String valueString -> statement.setString(i, valueString);
            case Integer valueInt -> statement.setInt(i, valueInt);
            case Double valueDouble -> statement.setDouble(i, valueDouble);
            case Long valueLong -> statement.setLong(i, valueLong);
            case Byte valueByte -> statement.setByte(i, valueByte);
            case Short valueShort -> statement.setShort(i, valueShort);
            case Float valueFloat -> statement.setFloat(i, valueFloat);
            case BigDecimal valueBigDecimal -> statement.setBigDecimal(i, valueBigDecimal);
            case Time valueTime -> statement.setTime(i, valueTime);
            case AsciiStream valueAsciiStream ->
                    statement.setAsciiStream(i, valueAsciiStream.x(), valueAsciiStream.length());
            case BinaryStream valueBinaryStream ->
                    statement.setBinaryStream(i, valueBinaryStream.x(), valueBinaryStream.length());
            case Blob valueBlob -> statement.setBlob(i, valueBlob);
            case Clob valueClob -> statement.setClob(i, valueClob);
            case Array valueArray -> statement.setArray(i, valueArray);
            case URL valueURL -> statement.setURL(i, valueURL);
            case null, default -> statement.setString(i, String.valueOf(value));
        }
    }

    /**
     * Executes an SQL query that may return a result set.
     *
     * @param connection the active SQL {@link Connection}.
     * @param query the SQL query string to be executed.
     * @param param a list of parameters (in the correct order) to be bound to the query.
     * @return a {@link ResultSet} if the query returns a result, or {@code null} otherwise.
     * @throws SQLException if an error occurs during the SQL execution.
     */
    public @Nullable ResultSet execute(Connection connection, String query, List<?> param) throws SQLException {
        PreparedStatement statement = this.getStatementFinal(connection, query, param).join();
        boolean hasResult = statement.execute();
        if (hasResult) {
            return statement.getResultSet();
        }
        return null;
    }
}
