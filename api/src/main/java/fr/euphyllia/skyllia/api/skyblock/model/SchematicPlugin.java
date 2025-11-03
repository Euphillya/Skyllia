package fr.euphyllia.skyllia.api.skyblock.model;

public enum SchematicPlugin {
    WORLD_EDIT,
    INTERNAL,
    UNKNOWN;

    public static SchematicPlugin fromString(String name) {
        if (name == null) {
            return UNKNOWN;
        }
        String lowerName = name.trim().toLowerCase();
        return switch (lowerName) {
            case "worldedit", "we", "fawe", "fastasyncworldedit" -> WORLD_EDIT;
            case "internal", "native", "json" -> INTERNAL;
            default -> UNKNOWN;
        };
    }
}
