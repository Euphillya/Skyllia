package fr.euphyllia.skyfolia.api.skyblock.model;

public record Position(int regionX, int regionZ) {

    @Override
    public String toString() {
        return "x=%s;z=%s".formatted(regionX, regionZ);
    }
}
