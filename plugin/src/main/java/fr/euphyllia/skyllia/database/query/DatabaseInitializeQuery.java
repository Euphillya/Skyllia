package fr.euphyllia.skyllia.database.query;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;

public abstract class DatabaseInitializeQuery {

    public abstract boolean init() throws DatabaseException;
}
