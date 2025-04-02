package fr.euphyllia.skyllia.sgbd.sqlite.configuration;

public record SQLiteConfig(String filePath, int minPool, int maxPool, long keepAliveTime, long maxLifetime,
                           int timeout) {
}
