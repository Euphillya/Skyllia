package fr.euphyllia.skyllia.managers;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;

public interface ConfigManager {
    void loadConfig() throws DatabaseException;
}
