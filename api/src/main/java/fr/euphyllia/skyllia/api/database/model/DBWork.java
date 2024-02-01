package fr.euphyllia.skyllia.api.database.model;

public interface DBWork {
    void run(java.sql.Connection connection);
}
