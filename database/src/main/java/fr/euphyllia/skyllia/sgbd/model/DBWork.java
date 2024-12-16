package fr.euphyllia.skyllia.sgbd.model;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;

public interface DBWork {
    void run(java.sql.Connection connection) throws DatabaseException;
}
