package fr.euphyllia.skyllia.api.world;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicSetting;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@ApiStatus.Internal
public interface WorldModifier {

    /**
     * Paste a schematic at a specific location.
     *
     * @param loc      The location where the schematic will be pasted.
     * @param settings The settings for the schematic paste operation.
     */
    void pasteSchematicWE(@NotNull Location loc, @NotNull SchematicSetting settings);

    /**
     * Delete an island by replacing its blocks with air.
     *
     * @param island         The island to be deleted.
     * @param world          The world where the island is located.
     * @param regionDistance The distance around the island to be cleared.
     * @param onFinish       A callback function that will be called with a boolean indicating success or failure when the operation is complete.
     */
    void deleteIsland(@NotNull Island island, @NotNull World world, int regionDistance, Consumer<Boolean> onFinish);

    /**
     * Change the biome of a specific chunk.
     *
     * @param location The location within the chunk to change the biome.
     * @param biome    The new biome to set for the chunk.
     * @return A CompletableFuture that will contain true if the operation was successful, false otherwise.
     */
    CompletableFuture<Boolean> changeBiomeChunk(@NotNull Location location, @NotNull Biome biome);

    /**
     * Change the biome of a specific chunk.
     *
     * @param world  The world where the chunk is located.
     * @param chunkX The X coordinate of the chunk.
     * @param chunkZ The Z coordinate of the chunk.
     * @param biome  The new biome to set for the chunk.
     * @return A CompletableFuture that will contain true if the operation was successful, false otherwise.
     */
    CompletableFuture<Boolean> changeBiomeChunk(@NotNull World world, int chunkX, int chunkZ, @NotNull Biome biome);

    /**
     * Change the biome of an entire island.
     *
     * @param world          The world where the island is located.
     * @param biome          The new biome to set for the island.
     * @param island         The island whose biome will be changed.
     * @param regionDistance The distance around the island to be affected.
     * @return A CompletableFuture that will contain true if the operation was successful, false otherwise.
     */
    CompletableFuture<Boolean> changeBiomeIsland(@NotNull World world, @NotNull Biome biome, @NotNull Island island, int regionDistance);


}
