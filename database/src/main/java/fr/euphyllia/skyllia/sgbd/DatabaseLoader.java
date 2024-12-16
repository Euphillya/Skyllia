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

public class DatabaseLoader {

    private final MariaDB mariaDB;

    public DatabaseLoader(MariaDB mariaDB) {
        this.mariaDB = mariaDB;
    }

    public boolean loadDatabase() throws DatabaseException {
        if (mariaDB != null && !mariaDB.isConnected()) {
            return mariaDB.onLoad();
        }
        return false;
    }

    public void closeDatabase() {
        if (mariaDB != null) {
            mariaDB.onClose();
        }
    }

    @Nullable
    public Connection getMariaDBConnection() throws DatabaseException {
        return mariaDB != null ? mariaDB.getConnection() : null;
    }

    public int executeInt(Connection connection, String query, List<?> param) throws SQLException {
        return this.getStatementFinal(connection, query, param).join().executeUpdate();
    }


    private CompletableFuture<PreparedStatement> getStatementFinal(Connection connection, String query, List<?> param) throws SQLException {
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
            completableFuture.complete(statement);
        }
        return completableFuture;
    }

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

    public @Nullable ResultSet execute(Connection connection, String query, List<?> param) throws SQLException {
        PreparedStatement statement = this.getStatementFinal(connection, query, param).join();
        boolean hasResult = statement.execute();
        if (hasResult) {
            return statement.getResultSet();
        }
        return null;
    }
}
