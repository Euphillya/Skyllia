package fr.euphyllia.skyllia.api;

import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.configuration.IConfigRegistry;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.coordinate.RegionCoordinate;
import fr.euphyllia.skyllia.api.database.IslandCustomDataQuery;
import fr.euphyllia.skyllia.api.permissions.IslandFlagRegistry;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.permissions.PermissionsManagers;
import fr.euphyllia.skyllia.api.permissions.modules.FlagModuleManager;
import fr.euphyllia.skyllia.api.permissions.modules.PermissionModuleManager;
import fr.euphyllia.skyllia.api.service.TrustService;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.IslandSettings;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.utils.nms.BiomesImpl;
import fr.euphyllia.skyllia.api.utils.nms.MobsSpawnImpl;
import fr.euphyllia.skyllia.api.utils.nms.WorldNMS;
import io.papermc.paper.ServerBuildInfo;
import net.kyori.adventure.key.Key;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * The SkylliaAPI class provides various methods to interact with Skyblock islands and check the server environment.
 */
public final class SkylliaAPI {

    private static final boolean IS_FOLIA;
    private static Plugin PLUGIN;
    private static SkylliaImplementation implementation;

    static {
        IS_FOLIA = ServerBuildInfo.buildInfo().isBrandCompatible(Key.key("papermc", "folia"));
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
    public static @Nullable Island getIslandByPlayerId(UUID playerUniqueId) {
        return implementation.getIslandByPlayerId(playerUniqueId);
    }

    /**
     * Retrieves the island associated with an island ID.
     *
     * @param islandId The UUID of the island.
     * @return A CompletableFuture that will contain the island associated with the island ID.
     */
    public static @Nullable Island getIslandByIslandId(UUID islandId) {
        return implementation.getIslandByIslandId(islandId);
    }

    /**
     * Retrieves the island owned by a specific player.
     *
     * @param playerUniqueId The UUID of the island owner.
     * @return The island owned by the specified player, or {@code null} if none is found.
     */
    public static @Nullable Island getIslandByOwner(UUID playerUniqueId) {
        return implementation.getIslandByOwner(playerUniqueId);
    }

    /**
     * Retrieves the island at a specific region coordinate.
     *
     * @param region The region coordinate to check.
     * @return The island at the specified region coordinate, or null if none is found.
     */
    public static @Nullable Island getIslandByRegion(RegionCoordinate region) {
        return implementation.getIslandByRegion(region);
    }

    /**
     * Retrieves the island at a specific position.
     *
     * @param position The position to check.
     * @return The island at the specified position, or null if none is found.
     * @deprecated Use {@link #getIslandByRegion(RegionCoordinate)} instead.
     */
    @Deprecated(forRemoval = true, since = "3.x")
    @ApiStatus.ScheduledForRemoval(inVersion = "4.x")
    public static @Nullable Island getIslandByRegion(Position position) {
        return implementation.getIslandByRegion(new RegionCoordinate(position.x(), position.z()));
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
     * Retrieves the list of all Skyblock world configurations registered in Skyllia.
     * <p>
     * A world is considered registered if it has been declared in Skyllia's
     * configuration. This does not guarantee that the world is currently
     * loaded or available in the server.
     * <p>
     * To check whether a specific world is a registered Skyblock world,
     * use {@link #isWorldSkyblock(String)} or {@link #isWorldSkyblock(World)}.
     *
     * @return An immutable list of {@link WorldConfig} representing all registered
     * Skyblock worlds. Returns an empty list if no worlds are configured.
     */
    public static List<WorldConfig> getRegisteredWorlds() {
        return implementation.getRegisteredWorlds();
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
    public static List<Island> getAllIslandsValid() {
        return implementation.getAllIslandsValid();
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

    @ApiStatus.Internal
    public static MobsSpawnImpl getMobsSpawnImpl() {
        return implementation.getMobsSpawnImpl();
    }

    /**
     * Retrieves the Permissions Manager.
     *
     * @return The PermissionsManagers instance.
     */
    public static PermissionsManagers getPermissionsManager() {
        return implementation.getPermissionsManager();
    }

    /**
     * Retrieves the Permission Module Manager.
     *
     * @return The PermissionModuleManager instance.
     */
    public static PermissionModuleManager getPermissionModuleManager() {
        return implementation.getPermissionModuleManager();
    }

    /**
     * Retrieves the Permission Registry.
     *
     * @return The PermissionRegistry instance.
     */
    public static PermissionRegistry getPermissionRegistry() {
        return implementation.getPermissionRegistry();
    }

    /**
     * Creates a new island with the specified {@link IslandSettings}.
     *
     * @param islandId The UUID of the new island.
     * @param settings The settings to apply to the new island.
     * @param owners   The owner of the island, must have {@link RoleType#OWNER}.
     * @return {@code true} if the island was successfully created,
     * {@code false} if creation was cancelled or an error occurred.
     * @throws IllegalArgumentException If any argument is null.
     * @throws IllegalStateException    If the owner's island ID is already set.
     */
    public static Boolean createIsland(UUID islandId, IslandSettings settings, Players owners) {
        return implementation.createIsland(islandId, settings, owners);
    }

    /**
     * Retrieves the Island Custom Data Query manager.
     * This allows addons to store custom persistent data associated with islands.
     *
     * @return The IslandCustomDataQuery implementation for database operations.
     */
    public static IslandCustomDataQuery getIslandCustomDataQuery() {
        return implementation.getIslandCustomDataQuery();
    }


    /**
     * Retrieves the Island Flag Registry.
     *
     * @return The IslandFlagRegistry instance.
     */
    public static IslandFlagRegistry getFlagRegistry() {
        return implementation.getFlagRegistry();
    }


    /**
     * Retrieves the Flag Module Manager.
     *
     * @return The FlagModuleManager instance.
     */
    public static FlagModuleManager getFlagModuleManager() {
        return implementation.getFlagModuleManager();
    }


    /**
     * Retrieves the configuration registry.
     * Addons can register their {@link fr.euphyllia.skyllia.api.configuration.IConfigurationProvider}
     * to participate in Skyllia's global reload cycle.
     *
     * @return The IConfigRegistry instance.
     */
    public static IConfigRegistry getConfigRegistry() {
        return implementation.getConfigRegistry();
    }

    /**
     * Retrieves the Trust Service.
     * <p>
     * The Trust Service manages trusted players on islands. A trusted player
     * is granted the same permissions as those defined for the
     * {@link RoleType#MEMBER} role, without officially being part of the island.
     *
     * @return The {@link TrustService} instance.
     */
    public static TrustService getTrustService() {
        return implementation.getTrustService();
    }
}
