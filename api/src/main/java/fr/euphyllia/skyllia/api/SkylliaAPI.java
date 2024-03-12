package fr.euphyllia.skyllia.api;

import fr.euphyllia.energie.Energie;
import fr.euphyllia.energie.model.Scheduler;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class SkylliaAPI {

    private static SkylliaImplementation implementation;
    private static Energie energie;

    public static void setImplementation(Plugin plugin, SkylliaImplementation skylliaImplementation) {
        implementation = skylliaImplementation;
        energie = new Energie(plugin);
    }

    public static CompletableFuture<@Nullable Island> getIslandByPlayerId(UUID playerUniqueId) {
        return implementation.getIslandByPlayerId(playerUniqueId);
    }

    public static @Nullable CompletableFuture<@Nullable Island> getIslandByIslandId(UUID islandId) {
        return implementation.getIslandByIslandId(islandId);
    }

    public static @Nullable Island getIslandByPosition(Position position) {
        return implementation.getIslandByPosition(position);
    }

    public static @Nullable Island getIslandByChunk(Chunk chunk) {
        return implementation.getIslandByChunk(chunk);
    }

    public static Scheduler getScheduler() {
        return energie.getScheduler(Energie.SchedulerSoft.MINECRAFT);
    }

    public static Scheduler getNativeScheduler() {
        return energie.getScheduler(Energie.SchedulerSoft.NATIVE);
    }

    public static boolean isFolia() {
        return Energie.isFolia();
    }

    public @NotNull Boolean isWorldSkyblock(String name) {
        return implementation.isWorldSkyblock(name);
    }

    public @NotNull Boolean isWorldSkyblock(World world) {
        return implementation.isWorldSkyblock(world);
    }
}
