package fr.euphyllia.skyllia.database.model;

public interface DBCallback {
    void run(java.sql.ResultSet resultSet);
}
