package fr.euphyllia.skyllia_insight_addon;

import dev.frankheijden.insights.api.addons.InsightsAddon;
import dev.frankheijden.insights.api.addons.Region;
import dev.frankheijden.insights.api.objects.chunk.ChunkLocation;
import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SkylliaInsightAddon implements InsightsAddon {

    @Override
    public String getPluginName() {
        return "Skyllia";
    }

    @Override
    public String getAreaName() {
        return "island";
    }

    @Override
    public String getVersion() {
        return "{version}";
    }

    @Override
    public Optional<Region> getRegion(org.bukkit.Location location) {
        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return Optional.empty();
        Island island = SkylliaAPI.getIslandByChunk(location.getChunk());
        if (island == null) return Optional.empty();
        return Optional.of(new SkylliaRegion(island, location.getWorld()));
    }

    public class SkylliaRegion implements Region {

        private final Island island;
        private final World world;
        public SkylliaRegion(Island island, World world) {
            this.island = island;
            this.world = world;
        }

        @Override
        public String getAddon() {
            return getPluginName();
        }

        @Override
        public String getKey() {
            return "SKYLLIA_%s".formatted(this.island.getId());
        }

        @Override
        public List<ChunkPart> toChunkParts() {
            List<ChunkPart> parts = new ArrayList<>();

            for (Position position : spiralStartCenter(island.getPosition(), island.getSize())) {
                parts.add(new ChunkPart(new ChunkLocation(world, position.x(), position.z())));
            }
            return parts;
        }


        public static List<Position> spiralStartCenter(Position islandRegion, double size) {
            List<Position> positions = new ArrayList<>();

            Position chunk = RegionHelper.getChunkCenterRegion(islandRegion.x(), islandRegion.z());
            int cx = chunk.x();
            int cz = chunk.z();
            int x = 0, z = 0;
            int dx = 0, dz = -1;
            int maxI = (int) Math.pow((33 * ConfigToml.regionDistance), 2);
            List<Position> islandPositionWithRadius = RegionHelper.getRegionsInRadius(islandRegion, (int) Math.round(size));
            List<Position> regionCleaned = new ArrayList<>();

            for (int i = 0; i < maxI; i++) {
                if ((-size / 2 <= x) && (x <= size / 2) && (-size / 2 <= z) && (z <= size / 2)) {
                    Position chunkPos = new Position(cx + x, cz + z);
                    Position region = RegionHelper.getRegionInChunk(chunkPos.x(), chunkPos.z());
                    if (islandPositionWithRadius.contains(region)) {
                        if (!regionCleaned.contains(region)) {
                            regionCleaned.add(region);
                        }
                        positions.add(chunkPos);
                    }
                }

                if ((x == z) || ((x < 0) && (x == -z)) || ((x > 0) && (x == 1 - z))) {
                    int temp = dx;
                    dx = -dz;
                    dz = temp;
                }
                x += dx;
                z += dz;
            }
            return positions;
        }
    }
}