package fr.euphyllia.skyllia.sgbd.model;

public interface DBCallback {
    void run(java.sql.ResultSet resultSet);
}
