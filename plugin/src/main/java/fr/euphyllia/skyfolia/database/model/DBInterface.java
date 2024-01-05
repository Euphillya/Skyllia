package fr.euphyllia.skyfolia.database.model;

public interface DBInterface {
    @org.jetbrains.annotations.Nullable
    java.sql.Connection getConnection();
}