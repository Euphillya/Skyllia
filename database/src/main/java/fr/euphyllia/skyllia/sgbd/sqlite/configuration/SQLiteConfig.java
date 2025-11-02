package fr.euphyllia.skyllia.sgbd.sqlite.configuration;

public record SQLiteConfig(String filePath, Number minPool, Number maxPool, Number keepAliveTime, Number maxLifetime,
                           Number timeout) {
}
