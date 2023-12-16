package fr.euphyllia.skyfolia.database.model;

public interface DBCallback {
    void run(java.sql.ResultSet resultSet);
}
