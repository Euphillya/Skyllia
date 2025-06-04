package fr.euphyllia.skyllia.api.skyblock.model;

import fr.euphyllia.skyllia.api.world.SkylliaLocation;

import java.util.UUID;

public record WarpIsland(UUID islandId, String warpName, SkylliaLocation location) {
}
