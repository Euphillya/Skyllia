package fr.euphyllia.skyllia.database.query;

import fr.euphyllia.sgbd.exceptions.DatabaseException;

public abstract class DatabaseInitializeQuery {

    public abstract boolean init() throws DatabaseException;
}
