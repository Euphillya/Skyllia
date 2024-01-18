package fr.euphyllia.skyllia.database.model;

public interface DBInterface {
    @org.jetbrains.annotations.Nullable
    java.sql.Connection getConnection();
}