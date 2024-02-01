package fr.euphyllia.skyllia.api.database.model;

import fr.euphyllia.skyllia.api.exceptions.DatabaseException;

public interface DBConnect {

    boolean onLoad() throws DatabaseException;

    void onClose();

    boolean isConnected();
}