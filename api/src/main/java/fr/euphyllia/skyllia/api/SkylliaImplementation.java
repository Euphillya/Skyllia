package fr.euphyllia.skyllia.api;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
    public CompletableFuture<@NotNull Island> getIslandByPlayerId(UUID playerUniqueId);

    /**
     * Retrieves the island associated with an island ID.
     *
     * @param islandId The UUID of the island.
     * @return A CompletableFuture that will contain the island associated with the island ID.
     */
    public CompletableFuture<@NotNull Island> getIslandByIslandId(UUID islandId);

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
}
