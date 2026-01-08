package fr.euphyllia.skyllia.managers.skyblock;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.SkylliaImplementation;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.permissions.PermissionsManagers;
import fr.euphyllia.skyllia.api.permissions.modules.PermissionModuleManager;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.api.utils.nms.BiomesImpl;
import fr.euphyllia.skyllia.api.utils.nms.WorldNMS;
import fr.euphyllia.skyllia.cache.island.IslandCache;
import fr.euphyllia.skyllia.cache.island.PlayersInIslandCache;
import fr.euphyllia.skyllia.cache.island.PositionIslandCache;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public final class APISkyllia implements SkylliaImplementation {

    private static final Logger log = LoggerFactory.getLogger(APISkyllia.class);
    private final InterneAPI interneAPI;

    public APISkyllia(InterneAPI interneAPI) {
        this.interneAPI = interneAPI;
    }

    @Override
    public @Nullable Island getIslandByPlayerId(UUID playerUniqueId) {
        return this.interneAPI.getSkyblockManager().getIslandByPlayerId(playerUniqueId);
    }

    /**
     * Retrieves the island associated with a player's UUID.
     *
     * @param playerUniqueId The UUID of the player.
     * @return A CompletableFuture that will contain the island associated with the player's UUID.
     */
    @Override
    public @Nullable Island getCacheIslandByPlayerId(UUID playerUniqueId) {
        UUID islandId = PlayersInIslandCache.getIslandIdByPlayer(playerUniqueId);
        if (islandId == null) {
            return null;
        }
        return IslandCache.getIsland(islandId);
    }

    @Override
    public @Nullable Island getIslandByIslandId(UUID islandId) {
        return this.interneAPI.getSkyblockManager().getIslandByIslandId(islandId);
    }

    /**
     * Retrieves the island associated with an island ID.
     *
     * @param islandId The UUID of the island.
     * @return An island associated with the island ID.
     */
    @Override
    public @Nullable Island getCacheIslandByIslandId(UUID islandId) {
        return IslandCache.getIsland(islandId);
    }

    @Override
    public @Nullable Island getIslandByPosition(Position position) {
        return PositionIslandCache.getIsland(position);
    }

    @Override
    public @Nullable Island getIslandByChunk(Chunk chunk) {
        Position position = RegionHelper.getRegionFromChunk(chunk.getX(), chunk.getZ());
        return PositionIslandCache.getIsland(position);
    }

    @Override
    public @Nullable Island getIslandByChunk(int chunkX, int chunkZ) {
        Position position = RegionHelper.getRegionFromChunk(chunkX, chunkZ);
        return PositionIslandCache.getIsland(position);
    }

    /**
     * Retrieves all valid (non-disabled) Skyllia islands from the database.
     *
     * @return A CompletableFuture containing a thread-safe list of active islands.
     */
    @Override
    public List<Island> getAllIslandsValid() {
        return this.interneAPI.getSkyblockManager().getAllIslandsValid();
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

    @Override
    public boolean registerCommands(SubCommandInterface commandInterface, String... commands) {
        try {
            this.interneAPI.getPlugin().getCommandRegistry().registerSubCommand(commandInterface, commands);
            return true;
        } catch (Exception exception) {
            log.error(exception.getMessage());
            return false;
        }
    }

    @Override
    public boolean registerAdminCommands(SubCommandInterface commandInterface, String... commands) {
        try {
            this.interneAPI.getPlugin().getAdminCommandRegistry().registerSubCommand(commandInterface, commands);
            return true;
        } catch (Exception exception) {
            log.error(exception.getMessage());
            return false;
        }
    }

    /**
     * Gets the average tick time for a specific location.
     *
     * @param location the location for which to get the average tick time
     * @return average tick time (5s, 15s, 1m, 5m, 15m in Folia-Server), or null if the region doesn't exist, or Minecraft average tick time (1m, 5m, 15m in Paper-Server)
     */
    @Override
    public double @Nullable [] getAverageTickTime(Location location) {
        if (SkylliaAPI.isFolia()) {
            return this.interneAPI.getWorldNMS().getAverageTickTimes(location);
        } else {
            double[] average = {};
            long[] times = Bukkit.getTickTimes();
            for (long time : times) {
                average = append(average, time / 1_000_000.0);
            }
            return average;
        }
    }

    /**
     * Gets the average tick time for a specific chunk.
     *
     * @param chunk the chunk for which to get the average tick time
     * @return average tick time (5s, 15s, 1m, 5m, 15m in Folia-Server), or null if the region doesn't exist, or Minecraft average tick time (1m, 5m, 15m in Paper-Server)
     */
    @Override
    public double @Nullable [] getAverageTickTime(Chunk chunk) {
        if (SkylliaAPI.isFolia()) {
            return this.interneAPI.getWorldNMS().getAverageTickTimes(chunk);
        } else {
            double[] average = {};
            long[] times = Bukkit.getTickTimes();
            for (long time : times) {
                average = append(average, time / 1_000_000.0);
            }
            return average;
        }
    }

    @Override
    public BiomesImpl getBiomesImpl() {
        return this.interneAPI.getBiomesImpl();
    }

    @Override
    public WorldNMS getWorldNMS() {
        return this.interneAPI.getWorldNMS();
    }

    @Override
    public PermissionsManagers getPermissionsManager() {
        return this.interneAPI.getManagers().getPermissionsManagers();
    }

    @Override
    public PermissionModuleManager getPermissionModuleManager() {
        return this.interneAPI.getManagers().getPermissionModuleManager();
    }

    @Override
    public PermissionRegistry getPermissionRegistry() {
        return this.interneAPI.getManagers().getPermissionRegistry();
    }


    private double[] append(double[] arr, double element) {
        double[] newArr = new double[arr.length + 1];
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        newArr[arr.length] = element;
        return newArr;
    }
}
