package fr.euphyllia.skyllia.api.configuration;

public record MariaDBConfig(DatabaseType databaseType, String hostname, String port, String user, String pass,
                            Boolean useSSL,
                            Integer maxPool, Integer timeOut, String database, int dbVersion) {
}
