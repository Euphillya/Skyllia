package fr.euphyllia.skyllia.sgbd.mariadb.configuration;

public record MariaDBConfig(String hostname, String port, String user, String pass,
                            Boolean useSSL,
                            Integer minPool, Integer maxPool, Long maxLifeTime, Long keepAliveTime, Integer timeOut,
                            String database) {
}
