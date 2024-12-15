package fr.euphyllia.skyllia.sgbd.model;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;

public interface DBInterface {
    @org.jetbrains.annotations.Nullable
    java.sql.Connection getConnection() throws DatabaseException;
}