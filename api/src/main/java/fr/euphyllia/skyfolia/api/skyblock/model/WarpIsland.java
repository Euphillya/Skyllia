package fr.euphyllia.skyfolia.api.skyblock.model;

import org.bukkit.Location;

import java.util.UUID;

public record WarpIsland(UUID islandId, String warpName, Location location) {
}
