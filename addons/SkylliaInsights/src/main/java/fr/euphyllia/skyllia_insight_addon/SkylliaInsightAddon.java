package fr.euphyllia.skyllia_insight_addon;

import dev.frankheijden.insights.api.addons.InsightsAddon;
import dev.frankheijden.insights.api.addons.Region;
import dev.frankheijden.insights.api.objects.chunk.ChunkLocation;
import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.coordinate.ChunkCoordinate;
import fr.euphyllia.skyllia.api.coordinate.RegionCoordinate;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SkylliaInsightAddon implements InsightsAddon {

    @Override
    public String getPluginName() {
        return Skyllia.getInstance().getName();
    }

    @Override
    public String getAreaName() {
        return Skyllia.getInstance().getName().toLowerCase();
    }

    @Override
    public String getVersion() {
        return Skyllia.getInstance().getPluginMeta().getVersion();
    }

    @Override
    public Optional<Region> getRegion(org.bukkit.Location location) {
        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return Optional.empty();
        final int chunkX = location.getBlockX() >> 4;
        final int chunkZ = location.getBlockZ() >> 4;
        final Island island = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
        if (island == null) return Optional.empty();
        return Optional.of(new SkylliaRegion(island, location.getWorld()));
    }

    public static class SkylliaRegion implements Region {

        private final Island island;
        private final World world;
        private List<ChunkPart> cachedParts = null;
        private double cachedSize = -1;

        public SkylliaRegion(Island island, World world) {
            this.island = island;
            this.world = world;
        }

        private static List<ChunkPart> buildChunkParts(Island island, World world) {
            RegionCoordinate islandRegion = island.getRegionCoordinate();
            double sizeInBlocks = island.getSize();
            ChunkCoordinate centerChunk = RegionHelper.getCenterChunkOfChunk(islandRegion.x(), islandRegion.z());
            int cx = centerChunk.x();
            int cz = centerChunk.z();

            int chunkRadius = (int) Math.ceil(sizeInBlocks / 16.0);

            List<ChunkPart> parts = new ArrayList<>((2 * chunkRadius + 1) * (2 * chunkRadius + 1));
            for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
                for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                    parts.add(new ChunkPart(new ChunkLocation(world, cx + dx, cz + dz)));
                }
            }
            return parts;
        }

        @Override
        public String getAddon() {
            return Skyllia.getInstance().getName();
        }

        @Override
        public String getKey() {
            return "SKYLLIA_%s".formatted(this.island.getId());
        }

        @Override
        public List<ChunkPart> toChunkParts() {
            double currentSize = island.getSize();
            if (cachedParts == null || currentSize != cachedSize) {
                cachedParts = buildChunkParts(island, world);
                cachedSize = currentSize;
            }
            return cachedParts;
        }
    }
}