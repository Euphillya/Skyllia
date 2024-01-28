package fr.euphyllia.skyllia.api.database;

import fr.euphyllia.skyllia.api.database.sgbd.MariaDB;
import fr.euphyllia.skyllia.api.database.stream.AsciiStream;
import fr.euphyllia.skyllia.api.database.stream.BinaryStream;
import fr.euphyllia.skyllia.api.exceptions.DatabaseException;
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
        if (mariaDB != null && mariaDB.isConnected()) {
            return mariaDB.getConnection();
        }
        return null;
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
        if (value instanceof byte[] valueBytes) {
            statement.setBytes(i, valueBytes);
        } else if (value instanceof Timestamp valueTimes) {
            statement.setTimestamp(i, valueTimes);
        } else if (value instanceof String valueString) {
            statement.setString(i, valueString);
        } else if (value instanceof Integer valueInt) {
            statement.setInt(i, valueInt);
        } else if (value instanceof Double valueDouble) {
            statement.setDouble(i, valueDouble);
        } else if (value instanceof Long valueLong) {
            statement.setLong(i, valueLong);
        } else if (value instanceof Byte valueByte) {
            statement.setByte(i, valueByte);
        } else if (value instanceof Short valueShort) {
            statement.setShort(i, valueShort);
        } else if (value instanceof Float valueFloat) {
            statement.setFloat(i, valueFloat);
        } else if (value instanceof BigDecimal valueBigDecimal) {
            statement.setBigDecimal(i, valueBigDecimal);
        } else if (value instanceof Time valueTime) {
            statement.setTime(i, valueTime);
        } else if (value instanceof AsciiStream valueAsciiStream) {
            statement.setAsciiStream(i, valueAsciiStream.x(), valueAsciiStream.length());
        } else if (value instanceof BinaryStream valueBinaryStream) {
            statement.setBinaryStream(i, valueBinaryStream.x(), valueBinaryStream.length());
        } else if (value instanceof Blob valueBlob) {
            statement.setBlob(i, valueBlob);
        } else if (value instanceof Clob valueClob) {
            statement.setClob(i, valueClob);
        } else if (value instanceof Array valueArray) {
            statement.setArray(i, valueArray);
        } else if (value instanceof URL valueURL) {
            statement.setURL(i, valueURL);
        } else {
            statement.setString(i, String.valueOf(value));
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
