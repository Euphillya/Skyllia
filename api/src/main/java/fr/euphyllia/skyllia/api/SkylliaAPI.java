package fr.euphyllia.skyllia.api;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.utils.scheduler.SchedulerTask;
import org.bukkit.Chunk;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class SkylliaAPI {

    private static SkylliaImplementation implementation;
    private static SchedulerTask schedulerTask;

    public static void setImplementation(Plugin plugin, SkylliaImplementation skylliaImplementation) {
        implementation = skylliaImplementation;
        schedulerTask = new SchedulerTask(plugin);
    }

    public static @Nullable CompletableFuture<@NotNull Island> getIslandByPlayerId(UUID playerUniqueId) {
        return implementation.getIslandByPlayerId(playerUniqueId);
    }

    public static @Nullable CompletableFuture<@NotNull Island> getIslandByIslandId(UUID islandId) {
        return implementation.getIslandByIslandId(islandId);
    }

    public static @Nullable Island getIslandByPosition(Position position) {
        return implementation.getIslandByPosition(position);
    }

    public static @Nullable Island getIslandByChunk(Chunk chunk) {
        return implementation.getIslandByChunk(chunk);
    }

    public static SchedulerTask getSchedulerTask() {
        return schedulerTask;
    }

    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
