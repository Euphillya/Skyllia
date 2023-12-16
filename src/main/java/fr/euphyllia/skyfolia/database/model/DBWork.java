package fr.euphyllia.skyfolia.database.model;

public interface DBWork {
    void run(java.sql.Connection connection);
}
