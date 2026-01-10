package fr.euphyllia.skyllia.api;

import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.permissions.PermissionsManagers;
import fr.euphyllia.skyllia.api.permissions.modules.PermissionModuleManager;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.utils.nms.BiomesImpl;
import fr.euphyllia.skyllia.api.utils.nms.WorldNMS;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * The SkylliaImplementation interface defines the methods that must be implemented
 * to interact with Skyblock islands and check Skyblock world status.
 */
public interface SkylliaImplementation {

    /**
     * Retrieves the island associated with a player's UUID.
     *
     * @param playerUniqueId The UUID of the player.
     * @return A CompletableFuture that will contain the island associated with the player's UUID.
     */
    public @Nullable Island getIslandByPlayerId(UUID playerUniqueId);

    /**
     * Retrieves the island associated with an island ID.
     *
     * @param islandId The UUID of the island.
     * @return A CompletableFuture that will contain the island associated with the island ID.
     */
    public @Nullable Island getIslandByIslandId(UUID islandId);

    /**
     * Retrieves the island owned by a specific owner.
     *
     * @param ownerId The UUID of the island owner.
     * @return The island owned by the specified owner, or null if none is found.
     */
    public @Nullable Island getIslandByOwner(UUID ownerId);

    /**
     * Retrieves the island at a specific position.
     *
     * @param position The position to check.
     * @return The island at the specified position, or null if none is found.
     */
    public @Nullable Island getIslandByPosition(Position position);

    /**
     * Retrieves the island associated with a specific chunk.
     *
     * @param chunk The chunk to check.
     * @return The island associated with the specified chunk, or null if none is found.
     */
    public @Nullable Island getIslandByChunk(Chunk chunk);

    /**
     * Retrieves the island associated with a specific chunk coordinates.
     *
     * @param chunkX The X coordinate of the chunk.
     * @param chunkZ The Z coordinate of the chunk.
     * @return The island associated with the specified chunk coordinates, or null if none is found.
     */
    public @Nullable Island getIslandByChunk(int chunkX, int chunkZ);

    /**
     * Retrieves all valid (non-disabled) Skyllia islands from the database.
     *
     * @return A CompletableFuture containing a thread-safe list of active islands.
     */
    public List<Island> getAllIslandsValid();

    /**
     * Checks if a world with the given name is a Skyblock world.
     *
     * @param name The name of the world.
     * @return True if the world is a Skyblock world, false otherwise.
     */
    public @NotNull Boolean isWorldSkyblock(String name);

    /**
     * Checks if the given world is a Skyblock world.
     *
     * @param world The world to check.
     * @return True if the world is a Skyblock world, false otherwise.
     */
    public @NotNull Boolean isWorldSkyblock(World world);

    /**
     * Gets the current location TPS.
     *
     * @param location the location for which to get the TPS
     * @return current location TPS (5s, 15s, 1m, 5m, 15m in Folia-Server), or null if the region doesn't exist, or Minecraft TPS (1m, 5m, 15m in Paper-Server)
     */
    public double @Nullable [] getTPS(Location location);

    /**
     * Gets the current chunk TPS.
     *
     * @param chunk the chunk for which to get the TPS
     * @return current location TPS (5s, 15s, 1m, 5m, 15m in Folia-Server), or null if the region doesn't exist, or Minecraft TPS (1m, 5m, 15m in Paper-Server)
     */
    public double @Nullable [] getTPS(Chunk chunk);

    /**
     * Registers commands with the provided command interface.
     *
     * @param commandInterface The command interface to use for the commands.
     * @param commands         The commands to register.
     * @return True if the commands were successfully registered, false otherwise.
     */
    public boolean registerCommands(SubCommandInterface commandInterface, String... commands);

    /**
     * Registers admin commands with the provided command interface.
     *
     * @param commandInterface The command interface to use for the admin commands.
     * @param commands         The admin commands to register.
     * @return True if the admin commands were successfully registered, false otherwise.
     */
    public boolean registerAdminCommands(SubCommandInterface commandInterface, String... commands);

    /**
     * Gets the average tick time for a specific location.
     *
     * @param location the location for which to get the average tick time
     * @return average tick time (5s, 15s, 1m, 5m, 15m in Folia-Server), or null if the region doesn't exist, or Minecraft average tick time (1m, 5m, 15m in Paper-Server)
     */
    double @Nullable [] getAverageTickTime(Location location);

    /**
     * Gets the average tick time for a specific chunk.
     *
     * @param chunk the chunk for which to get the average tick time
     * @return average tick time (5s, 15s, 1m, 5m, 15m in Folia-Server), or null if the region doesn't exist, or Minecraft average tick time (1m, 5m, 15m in Paper-Server)
     */
    double @Nullable [] getAverageTickTime(Chunk chunk);

    /**
     * Do not use. Reserved for Skyllia internal NMS bridges.
     */
    @ApiStatus.Internal
    BiomesImpl getBiomesImpl();

    /**
     * Do not use. Reserved for Skyllia internal NMS bridges.
     */
    @ApiStatus.Internal
    WorldNMS getWorldNMS();

    PermissionsManagers getPermissionsManager();

    PermissionModuleManager getPermissionModuleManager();

    PermissionRegistry getPermissionRegistry();

}
