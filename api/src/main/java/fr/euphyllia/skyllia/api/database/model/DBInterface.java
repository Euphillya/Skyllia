package fr.euphyllia.skyllia.api.database.model;

import fr.euphyllia.skyllia.api.exceptions.DatabaseException;

public interface DBInterface {
    @org.jetbrains.annotations.Nullable
    java.sql.Connection getConnection() throws DatabaseException;
}