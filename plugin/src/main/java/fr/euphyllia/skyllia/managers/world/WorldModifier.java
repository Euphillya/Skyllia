package fr.euphyllia.skyllia.managers.world;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.coordinate.ChunkCoordinate;
import fr.euphyllia.skyllia.api.coordinate.RegionCoordinate;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.utils.RegionUtils;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.configuration.manager.GeneralConfigManager;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@ApiStatus.Internal
public class WorldModifier {

    private static final Logger log = LoggerFactory.getLogger(WorldModifier.class);

    private final JavaPlugin plugin;
    private final ScheduledExecutorService deleteScheduler;
    private final ScheduledExecutorService biomeScheduler;

    public WorldModifier(JavaPlugin plugin) {
        this.plugin = plugin;
        GeneralConfigManager.ChunkProcessingSettings cfg = ConfigLoader.general.getIslandSettings().chunkProcessing();

        this.deleteScheduler = Executors.newScheduledThreadPool(
                cfg.resolvedDeleteThreads(),
                r -> {
                    Thread t = new Thread(r, "skyllia-delete-processor");
                    t.setDaemon(true);
                    t.setPriority(Thread.MIN_PRIORITY);
                    return t;
                }
        );

        this.biomeScheduler = Executors.newScheduledThreadPool(
                cfg.resolvedBiomeThreads(),
                r -> {
                    Thread t = new Thread(r, "skyllia-biome-processor");
                    t.setDaemon(true);
                    t.setPriority(Thread.MIN_PRIORITY);
                    return t;
                }
        );

        log.info("WorldModifier initialized — delete: {} thread(s) / {}ms delay, biome: {} thread(s) / {}ms delay",
                cfg.resolvedDeleteThreads(), cfg.deleteDelayMs(),
                cfg.resolvedBiomeThreads(), cfg.biomeDelayMs());
    }

    public void shutdown() {
        deleteScheduler.shutdown();
        biomeScheduler.shutdown();
    }

    public void deleteIsland(@NotNull Island island, @NotNull World world, int regionDistance, Consumer<Boolean> onFinish) {
        RegionCoordinate position = island.getRegionCoordinate();
        Biome defaultBiome = resolveDefaultBiome(world);
        List<ChunkCoordinate> chunks = RegionUtils.computeChunksToDelete(position, regionDistance, island.getSize());
        if (chunks.isEmpty()) {
            if (onFinish != null) onFinish.accept(true);
            return;
        }
        AtomicInteger toDelete = new AtomicInteger(chunks.size());
        AtomicBoolean failed = new AtomicBoolean(false);
        for (int i = 0; i < chunks.size(); i++) {
            final ChunkCoordinate chunkPos = chunks.get(i);
            final long delay = (long) i * ConfigLoader.general.getIslandSettings().chunkProcessing().deleteDelayMs();
            deleteScheduler.schedule(() -> {
                world.getChunkAtAsync(chunkPos.x(), chunkPos.z()).thenAccept(ignored -> {
                    try {
                        SkylliaAPI.getWorldNMS().resetChunk(world, chunkPos);
                        if (!SkylliaAPI.getBiomesImpl().setBiome(world, chunkPos.x(), chunkPos.z(), defaultBiome)) {
                            failed.set(true);
                        }
                    } catch (Exception e) {
                        failed.set(true);
                    }
                    if (toDelete.decrementAndGet() == 0 && onFinish != null) {
                        onFinish.accept(!failed.get());
                    }
                });
            }, delay, TimeUnit.MILLISECONDS);
        }
    }

    private Biome resolveDefaultBiome(@NotNull World world) {
        WorldConfig worldConfig = ConfigLoader.worldManager.getWorldConfig(world.getName());
        Biome fallback = switch (world.getEnvironment()) {
            case NETHER -> Biome.NETHER_WASTES;
            case THE_END -> Biome.THE_END;
            default -> Biome.PLAINS;
        };

        if (worldConfig == null || worldConfig.getBiomeId() == null || worldConfig.getBiomeId().isBlank()) {
            return fallback;
        }

        Biome configuredBiome = SkylliaAPI.getBiomesImpl().getBiome(worldConfig.getBiomeId());
        return configuredBiome != null ? configuredBiome : fallback;
    }

    public CompletableFuture<Boolean> changeBiomeChunk(@NotNull World world, int chunkX, int chunkZ, @NotNull Biome biome) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        world.getChunkAtAsync(chunkX, chunkZ).thenAccept(ignored -> {
                    try {
                        SkylliaAPI.getBiomesImpl().setBiome(world, chunkX, chunkZ, biome);
                        future.complete(true);
                    } catch (Exception e) {
                        future.complete(false);
                    }
                }
        );
        return future;
    }

    public CompletableFuture<Boolean> changeBiomeIsland(@NotNull World world, @NotNull Biome biome,
                                                        @NotNull Island island, int regionDistance) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        RegionCoordinate islandPos = island.getRegionCoordinate();
        List<ChunkCoordinate> chunks = new ArrayList<>();
        RegionUtils.spiralStartCenter(islandPos, regionDistance, island.getSize(), chunks::add);
        if (chunks.isEmpty()) {
            future.complete(true);
            return future;
        }
        AtomicInteger remaining = new AtomicInteger(chunks.size());
        AtomicBoolean failed = new AtomicBoolean(false);
        for (int i = 0; i < chunks.size(); i++) {
            ChunkCoordinate chunk = chunks.get(i);
            final int cX = chunk.x();
            final int cZ = chunk.z();
            final long delay = (long) i * ConfigLoader.general.getIslandSettings().chunkProcessing().biomeDelayMs();
            biomeScheduler.schedule(() -> {
                world.getChunkAtAsync(cX, cZ).thenAccept(ignored -> {
                    try {
                        SkylliaAPI.getBiomesImpl().setBiome(world, cX, cZ, biome);
                    } catch (Exception e) {
                        failed.set(true);
                    } finally {
                        if (remaining.decrementAndGet() == 0) {
                            future.complete(!failed.get());
                        }
                    }
                });
            }, delay, TimeUnit.MILLISECONDS);
        }
        return future;
    }
}
