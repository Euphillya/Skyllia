package fr.euphyllia.skyllia.sgbd.model;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;

public interface DBConnect {

    boolean onLoad() throws DatabaseException;

    void onClose();

    boolean isConnected();
}