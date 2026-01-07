package fr.euphyllia.skyllia.api;

import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.permissions.PermissionsManagers;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.utils.nms.BiomesImpl;
import fr.euphyllia.skyllia.api.utils.nms.WorldNMS;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The SkylliaAPI class provides various methods to interact with Skyblock islands and check the server environment.
 */
public final class SkylliaAPI {

    private static final boolean IS_FOLIA;
    private static Plugin PLUGIN;
    private static SkylliaImplementation implementation;

    static {
        IS_FOLIA = hasClass("io.papermc.paper.threadedregions.RegionizedServer");
    }

    /**
     * Sets the implementation for the SkylliaAPI.
     *
     * @param plugin                The plugin instance.
     * @param skylliaImplementation The implementation of the SkylliaAPI.
     */
    @ApiStatus.Internal
    public static void setImplementation(Plugin plugin, SkylliaImplementation skylliaImplementation) {
        PLUGIN = plugin;
        implementation = skylliaImplementation;
    }

    /**
     * Retrieves the island associated with a player's UUID.
     *
     * @param playerUniqueId The UUID of the player.
     * @return A CompletableFuture that will contain the island associated with the player's UUID.
     */
    public static CompletableFuture<@Nullable Island> getIslandByPlayerId(UUID playerUniqueId) {
        return implementation.getIslandByPlayerId(playerUniqueId);
    }

    /**
     * Retrieves the island associated with an island ID.
     *
     * @param islandId The UUID of the island.
     * @return A CompletableFuture that will contain the island associated with the island ID.
     */
    public static CompletableFuture<@Nullable Island> getIslandByIslandId(UUID islandId) {
        return implementation.getIslandByIslandId(islandId);
    }

    /**
     * Retrieves the island at a specific position.
     *
     * @param position The position to check.
     * @return The island at the specified position, or null if none is found.
     */
    public static @Nullable Island getIslandByPosition(Position position) {
        return implementation.getIslandByPosition(position);
    }

    /**
     * Retrieves the island associated with a specific chunk.
     *
     * @param chunk The chunk to check.
     * @return The island associated with the specified chunk, or null if none is found.
     */
    public static @Nullable Island getIslandByChunk(Chunk chunk) {
        return implementation.getIslandByChunk(chunk);
    }

    /**
     * Retrieves the island associated with a specific chunk.
     *
     * @param chunkX The X coordinate of the chunk.
     * @param chunkZ The Z coordinate of the chunk.
     * @return The island associated with the specified chunk, or null if none is found.
     */
    public static @Nullable Island getIslandByChunk(int chunkX, int chunkZ) {
        return implementation.getIslandByChunk(chunkX, chunkZ);
    }

    /**
     * Checks if the server is running on Folia.
     *
     * @return True if the server is running on Folia, false otherwise.
     */
    public static boolean isFolia() {
        return IS_FOLIA;
    }

    /**
     * Checks if a world with the given name is a Skyblock world.
     *
     * @param name The name of the world.
     * @return True if the world is a Skyblock world, false otherwise.
     */
    public static @NotNull Boolean isWorldSkyblock(String name) {
        return implementation.isWorldSkyblock(name);
    }

    /**
     * Checks if the given world is a Skyblock world.
     *
     * @param world The world to check.
     * @return True if the world is a Skyblock world, false otherwise.
     */
    public static @NotNull Boolean isWorldSkyblock(World world) {
        return implementation.isWorldSkyblock(world);
    }

    /**
     * Retrieves the plugin instance associated with this API.
     *
     * @return The plugin instance.
     */
    public static Plugin getPlugin() {
        return PLUGIN;
    }

    /**
     * Checks if a class with the specified name exists in the classpath.
     *
     * @param className The name of the class to check.
     * @return True if the class exists, false otherwise.
     */
    private static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException var2) {
            return false;
        }
    }

    /**
     * Gets the current location TPS.
     *
     * @param location the location for which to get the TPS
     * @return current location TPS (5s, 15s, 1m, 5m, 15m in Folia-Server), or null if the region doesn't exist, or Minecraft TPS (1m, 5m, 15m in Paper-Server)
     */
    public static double @Nullable [] getTPS(Location location) {
        return implementation.getTPS(location);
    }

    /**
     * Gets the current chunk TPS.
     *
     * @param chunk the chunk for which to get the TPS
     * @return current location TPS (5s, 15s, 1m, 5m, 15m in Folia-Server), or null if the region doesn't exist, or Minecraft TPS (1m, 5m, 15m in Paper-Server)
     */
    public static double @Nullable [] getTPS(Chunk chunk) {
        return implementation.getTPS(chunk);
    }


    /**
     * Gets the average tick time for a specific location.
     *
     * @param location the location for which to get the average tick time
     * @return average tick time (5s, 15s, 1m, 5m, 15m in Folia-Server), or null if the region doesn't exist, or Minecraft average tick time (1m, 5m, 15m in Paper-Server)
     */
    public static double @Nullable [] getAverageTickTime(Location location) {
        return implementation.getAverageTickTime(location);
    }

    /**
     * Gets the average tick time for a specific chunk.
     *
     * @param chunk the chunk for which to get the average tick time
     * @return average tick time (5s, 15s, 1m, 5m, 15m in Folia-Server), or null if the region doesn't exist, or Minecraft average tick time (1m, 5m, 15m in Paper-Server)
     */
    public static double @Nullable [] getAverageTickTime(Chunk chunk) {
        return implementation.getAverageTickTime(chunk);
    }

    /**
     * Retrieves all valid (non-disabled) Skyllia islands from the database.
     *
     * @return A CompletableFuture containing a thread-safe list of active islands.
     */
    public static CompletableFuture<CopyOnWriteArrayList<Island>> getAllIslandsValid() {
        return implementation.getAllIslandsValid();
    }

    /**
     * Retrieves the island associated with a player's UUID.
     *
     * @param playerUniqueId The UUID of the player.
     * @return An island associated with the player's UUID.
     */
    public static @Nullable Island getCacheIslandByPlayerId(UUID playerUniqueId) {
        return implementation.getCacheIslandByPlayerId(playerUniqueId);
    }

    /**
     * Retrieves the island associated with an island ID.
     *
     * @param islandId The UUID of the island.
     * @return An island associated with the island ID.
     */
    public static @Nullable Island getCacheIslandByIslandId(UUID islandId) {
        return implementation.getCacheIslandByIslandId(islandId);
    }

    /**
     * Registers commands with the provided command interface.
     *
     * @param commandInterface The command interface to use for the commands.
     * @param commands         The commands to register.
     * @return True if the commands were successfully registered, false otherwise.
     */
    public static boolean registerCommands(SubCommandInterface commandInterface, String... commands) {
        return implementation.registerCommands(commandInterface, commands);
    }

    /**
     * Registers admin commands with the provided command interface.
     *
     * @param commandInterface The command interface to use for the admin commands.
     * @param commands         The admin commands to register.
     * @return True if the admin commands were successfully registered, false otherwise.
     */
    public static boolean registerAdminCommands(SubCommandInterface commandInterface, String... commands) {
        return implementation.registerAdminCommands(commandInterface, commands);
    }

    /**
     * Do not use. Reserved for Skyllia internal NMS bridges.
     */
    @ApiStatus.Internal
    public static BiomesImpl getBiomesImpl() {
        return implementation.getBiomesImpl();
    }

    /**
     * Do not use. Reserved for Skyllia internal NMS bridges.
     */
    @ApiStatus.Internal
    public static WorldNMS getWorldNMS() {
        return implementation.getWorldNMS();
    }

    public static PermissionsManagers getPermissionsManager() {
        return implementation.getPermissionsManager();
    }
}
