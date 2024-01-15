package fr.euphyllia.skyfolia.api.skyblock.model;

import java.util.Map;

public record IslandStarter(String name, Map<String, SchematicWorld> schematicWorldMap) {
}
