package fr.euphyllia.skyllia.database.model;

public interface DBWork {
    void run(java.sql.Connection connection);
}
