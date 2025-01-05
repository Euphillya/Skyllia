package fr.euphyllia.skyllia.sgbd.configuration;

public record MariaDBConfig(String hostname, String port, String user, String pass,
                            Boolean useSSL,
                            Integer maxPool, Integer minPool, Integer timeOut, Long maxLifeTime, Long keepAliveTime,
                            String database, int dbVersion) {
}
