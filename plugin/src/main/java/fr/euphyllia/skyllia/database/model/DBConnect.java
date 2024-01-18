package fr.euphyllia.skyllia.database.model;

public interface DBConnect {

    boolean onLoad();

    void onClose();

    boolean isConnected();
}