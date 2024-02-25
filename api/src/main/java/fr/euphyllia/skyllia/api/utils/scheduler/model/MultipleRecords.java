package fr.euphyllia.skyllia.api.utils.scheduler.model;

import org.bukkit.World;

public class MultipleRecords {
    public record WorldChunk(World world, int chunkX, int chunkZ) {
    }
}
