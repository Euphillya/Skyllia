package fr.euphyllia.skyllia.api;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface SkylliaImplementation {

    public @Nullable CompletableFuture<@NotNull Island> getIslandByPlayerId(UUID playerUniqueId);

    public CompletableFuture<@NotNull Island> getIslandByIslandId(UUID islandId);

    public @NotNull Island getIslandByPosition(Position position);

    public @NotNull Island getIslandByChunk(Chunk chunk);
}
