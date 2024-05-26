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

public final class SkylliaAPI {

    private static final boolean IS_FOLIA;
    private static Plugin PLUGIN;
    private static SkylliaImplementation implementation;

    static {
        IS_FOLIA = hasClass("io.papermc.paper.threadedregions.RegionizedServer");
    }


    public static void setImplementation(Plugin plugin, SkylliaImplementation skylliaImplementation) {
        PLUGIN = plugin;
        implementation = skylliaImplementation;
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

    public static boolean isFolia() {
        return IS_FOLIA;
    }

    public static @NotNull Boolean isWorldSkyblock(String name) {
        return implementation.isWorldSkyblock(name);
    }

    public static @NotNull Boolean isWorldSkyblock(World world) {
        return implementation.isWorldSkyblock(world);
    }

    public static Plugin getPlugin() {
        return PLUGIN;
    }

    private static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException var2) {
            return false;
        }
    }
}
