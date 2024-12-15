package fr.euphyllia.skyllia.sgbd.model;

public interface DBWork {
    void run(java.sql.Connection connection);
}
