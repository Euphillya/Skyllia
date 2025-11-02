package fr.euphyllia.skyllia.sgbd.mariadb.configuration;

public record MariaDBConfig(String hostname, String port, String user, String pass,
                            Boolean useSSL,
                            Number minPool, Number maxPool, Number maxLifeTime, Number keepAliveTime, Number timeOut,
                            String database) {
}
