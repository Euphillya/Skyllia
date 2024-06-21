package fr.euphyllia.skyllia.api;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
     * @param plugin The plugin instance.
     * @param skylliaImplementation The implementation of the SkylliaAPI.
     */
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
    public static @Nullable CompletableFuture<@Nullable Island> getIslandByIslandId(UUID islandId) {
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
}
