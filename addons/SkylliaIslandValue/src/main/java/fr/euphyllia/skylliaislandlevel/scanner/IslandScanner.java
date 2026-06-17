package fr.euphyllia.skylliaislandlevel.scanner;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.coordinate.RegionCoordinate;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skylliaislandlevel.SkylliaIslandLevel;
import fr.euphyllia.skylliaislandlevel.configuration.IslandLevelConfigLoader;
import fr.euphyllia.skylliaislandlevel.configuration.IslandLevelConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

public class IslandScanner {

    private static final Logger log = LoggerFactory.getLogger(IslandScanner.class);
    private final SkylliaIslandLevel plugin;
    private final ScheduledExecutorService scanScheduler;
    private final ExecutorService scoreExecutor;

    public IslandScanner(SkylliaIslandLevel plugin) {
        this.plugin = plugin;
        IslandLevelConfigManager.ChunkProcessingSettings cfg = IslandLevelConfigLoader.config.getProcessingSettings();
        this.scanScheduler = Executors.newScheduledThreadPool(
                cfg.resolvedScanThreads(),
                r -> {
                    Thread t = new Thread(r, "skyllia-scan-scheduler");
                    t.setDaemon(true);
                    t.setPriority(Thread.MIN_PRIORITY);
                    return t;
                });

        this.scoreExecutor = Executors.newFixedThreadPool(
                Math.max(1, cfg.resolvedScoreThreads()),
                r -> {
                    Thread t = new Thread(r, "skyllia-scan-scorer");
                    t.setDaemon(true);
                    t.setPriority(Thread.MIN_PRIORITY);
                    return t;
                });
    }

    public CompletableFuture<Double> scanIsland(Island island, World world) {
        CompletableFuture<Double> result = new CompletableFuture<>();

        List<int[]> chunkCoords = getIslandChunks(island);

        // DEBUG
        log.debug("[Scanner] Island {} — center chunk ({},{}) size={} → {} chunks to scan",
                island.getId(),
                island.getRegionCoordinate().x(), island.getRegionCoordinate().z(),
                island.getSize(),
                chunkCoords.size());

        if (chunkCoords.isEmpty()) {
            result.complete(0.0);
            return result;
        }

        Map<Material, Double> blockValues = IslandLevelConfigLoader.config.getBlockValues();
        log.debug("[Scanner] blockValues loaded: {} entries", blockValues.size());
        List<Player> onlineMembers = new ArrayList<>();
        for (Players member : island.getMembers()) {
            Player player = Bukkit.getPlayer(member.getMojangId());
            if (player != null) {
                onlineMembers.add(player);
            }
        }

        ScanProgressNotifier notifier = onlineMembers.isEmpty()
                ? null
                : new ScanProgressNotifier(onlineMembers);

        int total = chunkCoords.size();
        AtomicInteger remaining = new AtomicInteger(total);
        AtomicInteger done = new AtomicInteger(0);
        DoubleAdder totalScore = new DoubleAdder();

        IslandLevelConfigManager.ChunkProcessingSettings cfg =
                IslandLevelConfigLoader.config.getProcessingSettings();

        int maxConcurrent = Math.max(1, cfg.resolvedScoreThreads() * 2);
        Semaphore semaphore = new Semaphore(maxConcurrent);

        for (int i = 0; i < chunkCoords.size(); i++) {
            int[] coord = chunkCoords.get(i);
            int chunkX = coord[0];
            int chunkZ = coord[1];
            final long delay = (long) cfg.scanDelayMs() * i;

            this.scanScheduler.schedule(() -> {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    if (remaining.decrementAndGet() == 0) result.complete(totalScore.sum());
                    return;
                }

                world.getChunkAtAsync(chunkX, chunkZ, false, false)
                        .thenAcceptAsync(chunk -> {
                            if (chunk == null) {
                                semaphore.release();
                                if (notifier != null) notifier.update(done.incrementAndGet(), total);
                                if (remaining.decrementAndGet() == 0) {
                                    if (notifier != null) notifier.finish();
                                    result.complete(totalScore.sum());
                                }
                                return;
                            }
                            try {
                                double chunkScore = scoreChunk(world, chunkX, chunkZ, blockValues);
                                totalScore.add(chunkScore);
                            } catch (Throwable e) {
                                log.error("Failed to score chunk ({},{}) for island '{}'",
                                        chunkX, chunkZ, island.getId(), e);
                            } finally {
                                semaphore.release();
                                int nowDone = done.incrementAndGet();
                                if (notifier != null) notifier.update(nowDone, total);
                                if (remaining.decrementAndGet() == 0) {
                                    if (notifier != null) notifier.finish();
                                    result.complete(totalScore.sum());
                                }
                            }
                        }, scoreExecutor);
            }, delay, TimeUnit.MILLISECONDS);

        }
        return result;
    }

    private double scoreChunk(World world, int chunkX, int chunkZ, Map<Material, Double> blockValues) {
        Map<Material, Integer> blockCounts = SkylliaAPI.getWorldNMS().getCountAllBlocksInChunk(world, chunkX, chunkZ);

        if (!blockCounts.isEmpty()) {
            log.debug("[Scanner] Chunk ({},{}) → {} block types found, sample: {}",
                    chunkX, chunkZ, blockCounts.size(),
                    blockCounts.entrySet().stream().limit(5)
                            .map(e -> e.getKey() + "×" + e.getValue())
                            .collect(java.util.stream.Collectors.joining(", ")));
        } else {
            log.debug("[Scanner] Chunk ({},{}) → empty", chunkX, chunkZ);
        }

        if (blockCounts.isEmpty()) return 0.0;

        double score = 0.0;
        for (Map.Entry<Material, Integer> entry : blockCounts.entrySet()) {
            Double value = blockValues.get(entry.getKey());
            if (value != null) {
                score += value * entry.getValue();
            }
        }

        if (score > 0) log.debug("[Scanner] Chunk ({},{}) → score={}", chunkX, chunkZ, score);

        return score;
    }

    private List<int[]> getIslandChunks(Island island) {
        RegionCoordinate pos = island.getRegionCoordinate();
        double size = island.getSize();

        int centerChunkX = (pos.x() << 5) + 16;
        int centerChunkZ = (pos.z() << 5) + 16;

        int chunkRadius = (int) Math.ceil(size / 16.0);

        List<int[]> result = new ArrayList<>();
        for (int cx = centerChunkX - chunkRadius; cx <= centerChunkX + chunkRadius; cx++) {
            for (int cz = centerChunkZ - chunkRadius; cz <= centerChunkZ + chunkRadius; cz++) {
                result.add(new int[]{cx, cz});
            }
        }
        return result;
    }

    public void shutdown() {
        scanScheduler.shutdown();
        scoreExecutor.shutdown();
    }
}
