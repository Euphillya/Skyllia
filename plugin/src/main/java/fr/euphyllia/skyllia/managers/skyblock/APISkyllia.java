package fr.euphyllia.skyllia.managers.skyblock;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.SkylliaImplementation;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.cache.PositionIslandCache;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class APISkyllia implements SkylliaImplementation {

    private final InterneAPI interneAPI;

    public APISkyllia(InterneAPI interneAPI) {
        this.interneAPI = interneAPI;
    }

    @Override
    public CompletableFuture<@Nullable Island> getIslandByPlayerId(UUID playerUniqueId) {
        return this.interneAPI.getSkyblockManager().getIslandByPlayerId(playerUniqueId);
    }

    @Override
    public CompletableFuture<@Nullable Island> getIslandByIslandId(UUID islandId) {
        return this.interneAPI.getSkyblockManager().getIslandByIslandId(islandId);
    }

    @Override
    public @Nullable Island getIslandByPosition(Position position) {
        return PositionIslandCache.getIsland(position);
    }

    @Override
    public @Nullable Island getIslandByChunk(Chunk chunk) {
        Position position = RegionHelper.getRegionInChunk(chunk.getX(), chunk.getZ());
        return PositionIslandCache.getIsland(position);
    }

    @Override
    public @NotNull Boolean isWorldSkyblock(String name) {
        return WorldUtils.isWorldSkyblock(name);
    }

    @Override
    public @NotNull Boolean isWorldSkyblock(World world) {
        return WorldUtils.isWorldSkyblock(world.getName());
    }

    /**
     * Gets the current location TPS.
     *
     * @param location the location for which to get the TPS
     * @return current location TPS (5s, 15s, 1m, 5m, 15m in Folia-Server), or null if the region doesn't exist, or Minecraft TPS (1m, 5m, 15m in Paper-Server)
     */
    @Override
    public double @Nullable [] getTPS(Location location) {
        if (SkylliaAPI.isFolia()) {
            return this.interneAPI.getWorldNMS().getTPS(location);
        } else {
            return Bukkit.getTPS();
        }
    }

    /**
     * Gets the current chunk TPS.
     *
     * @param chunk the chunk for which to get the TPS
     * @return current location TPS (5s, 15s, 1m, 5m, 15m in Folia-Server), or null if the region doesn't exist, or Minecraft TPS (1m, 5m, 15m in Paper-Server)
     */
    @Override
    public double @Nullable [] getTPS(Chunk chunk) {
        if (SkylliaAPI.isFolia()) {
            return this.interneAPI.getWorldNMS().getTPS(chunk);
        } else {
            return Bukkit.getTPS();
        }
    }
}
