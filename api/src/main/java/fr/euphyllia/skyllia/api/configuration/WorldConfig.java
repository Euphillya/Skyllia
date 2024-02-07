package fr.euphyllia.skyllia.api.configuration;

public record WorldConfig(String name, String environment, PortalConfig netherPortal,
                          PortalConfig endPortal) {
}
