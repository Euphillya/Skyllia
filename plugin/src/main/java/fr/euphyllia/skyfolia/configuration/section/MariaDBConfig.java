package fr.euphyllia.skyfolia.configuration.section;

public record MariaDBConfig(String hostname, String port, String user, String pass, Boolean useSSL,
                            Integer maxPool, Integer timeOut, String database) {
}
