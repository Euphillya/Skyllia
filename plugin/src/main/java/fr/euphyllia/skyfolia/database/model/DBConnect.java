package fr.euphyllia.skyfolia.database.model;

public interface DBConnect {

    boolean onLoad();

    void onClose();

    boolean isConnected();
}