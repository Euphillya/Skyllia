package fr.euphyllia.skyllia.api.world;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicSetting;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface WorldModifier  {

    public void pasteSchematicWE(@NotNull Location loc, @NotNull SchematicSetting settings);

    public void deleteIsland(@NotNull Island island, @NotNull World world, int regionDistance, Consumer<Boolean> onFinish);

    public CompletableFuture<Boolean> changeBiomeChunk(@NotNull Location location, @NotNull Biome biome);

    public CompletableFuture<Boolean> changeBiomeChunk(@NotNull World world, int chunkX, int chunkZ, @NotNull Biome biome);

    public CompletableFuture<Boolean> changeBiomeIsland(@NotNull World world, @NotNull Biome biome, @NotNull Island island, int regionDistance);



}
