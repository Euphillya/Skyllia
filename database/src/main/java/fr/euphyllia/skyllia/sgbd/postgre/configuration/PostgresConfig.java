package fr.euphyllia.skyllia.sgbd.postgre.configuration;

public record PostgresConfig(
        String typeDB, String hostname, Number port, String user, String pass,
        Boolean useSSL,
        Number minPool, Number maxPool, Number maxLifeTime, Number keepAliveTime, Number timeOut,
        String database, String prefix
) {
}