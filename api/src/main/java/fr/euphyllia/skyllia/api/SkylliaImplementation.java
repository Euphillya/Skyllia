package fr.euphyllia.skyllia.api;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface SkylliaImplementation {

    public CompletableFuture<@NotNull Island> getIslandByPlayerId(UUID playerUniqueId);

    public CompletableFuture<@NotNull Island> getIslandByIslandId(UUID islandId);

    public @Nullable Island getIslandByPosition(Position position);

    public @Nullable Island getIslandByChunk(Chunk chunk);

    public @NotNull Boolean isWorldSkyblock(String name);

    public @NotNull Boolean isWorldSkyblock(World world);
}
