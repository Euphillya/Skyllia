package fr.euphyllia.skyllia.sgbd;

import org.jetbrains.annotations.Nullable;

public record DatabaseConfig(String hostname, String port, String user, String pass,
                             Boolean useSSL,
                             Integer minPool, Integer maxPool, Long maxLifeTime, Long keepAliveTime, Integer timeOut,
                             String database, @Nullable String filePath) {
}
