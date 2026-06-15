package fr.euphyllia.skylliaore.listeners;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliaore.SkylliaOre;
import fr.euphyllia.skylliaore.api.Generator;
import fr.euphyllia.skylliaore.hook.CraftEngineHook;
import fr.euphyllia.skylliaore.hook.NexoHook;
import fr.euphyllia.skylliaore.hook.OraxenHook;
import fr.euphyllia.skylliaore.utils.OptimizedGenerator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class OreEvent implements Listener {

    private static final Logger log = LoggerFactory.getLogger(OreEvent.class);

    private static final boolean isOraxenLoaded = SkylliaOre.isOraxenLoaded();
    private static final boolean isNexoLoaded = SkylliaOre.isNexoLoaded();
    private static final boolean isCraftEngineLoaded = SkylliaOre.isCraftEngineLoaded();

    private static final ConcurrentHashMap<String, BlockData> blockDataCache = new ConcurrentHashMap<>();
    private static final BlockData defaultBlockData = Material.COBBLESTONE.createBlockData();

    @EventHandler
    public void onBlockForm(final BlockFormEvent event) {
        if (event.isCancelled()) return;

        Block block = event.getBlock();
        Location location = block.getLocation();
        World world = location.getWorld();
        if (!SkylliaAPI.isWorldSkyblock(world)) return;

        final int chunkX = location.getBlockX() >> 4;
        final int chunkZ = location.getBlockZ() >> 4;
        final Island island = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
        if (island == null) return;

        handleBlockFormation(event, world, island);
    }

    private void handleBlockFormation(BlockFormEvent event, World world, Island island) {
        String worldName = world.getName().toLowerCase();
        Material blockType = event.getNewState().getType();

        Generator generator = SkylliaOre.getCachedGenerator(island.getId());
        if (generator == null) return;

        OptimizedGenerator optimized = SkylliaOre.getInstance().getOreCache().getOrBuildOptimized(generator);

        if (optimized.getGenerator().worlds().contains(worldName)) {
            String blockName = blockType.name().toLowerCase();
            if (optimized.getGenerator().replaceBlocks().contains(blockName)) {
                BlockData blockByChance = getBlockByChance(optimized);
                event.getNewState().setBlockData(blockByChance);
            }
        }
    }

    private BlockData getBlockByChance(OptimizedGenerator optimizedGenerator) {
        double randomChance = ThreadLocalRandom.current().nextDouble() * optimizedGenerator.getTotalChance();
        List<OptimizedGenerator.BlockProbability> cumulativeProbabilities = optimizedGenerator.getCumulativeProbabilities();

        int index = Collections.binarySearch(cumulativeProbabilities, new OptimizedGenerator.BlockProbability("", randomChance),
                Comparator.comparingDouble(OptimizedGenerator.BlockProbability::cumulativeChance));

        if (index < 0) {
            index = -index - 1;
        }

        if (index >= 0 && index < cumulativeProbabilities.size()) {
            return getCachedBlockData(cumulativeProbabilities.get(index).blockKey());
        }

        return defaultBlockData;
    }

    private BlockData getCachedBlockData(String key) {
        return blockDataCache.computeIfAbsent(key, k -> {
            try {
                if (k.startsWith("oraxen:") && isOraxenLoaded) {
                    BlockData data = OraxenHook.getBlockData(k.substring("oraxen:".length()));
                    if (data != null) return data;
                } else if (k.startsWith("nexo:") && isNexoLoaded) {
                    BlockData data = NexoHook.getBlockData(k.substring("nexo:".length()));
                    if (data != null) return data;
                } else if (k.startsWith("craftengine:") && isCraftEngineLoaded) {
                    // "craftengine:ore:bcop2" -> "ore:bcop2"
                    String ceKey = k.substring("craftengine:".length());
                    BlockData data = CraftEngineHook.getBlockData(ceKey);
                    if (data != null) return data;
                    log.warn("CraftEngine block not found: '{}'", ceKey);
                    return defaultBlockData;
                } else if (k.contains(":") && !k.startsWith("minecraft:")) {
                    log.warn("'{}' is namespaced but has no provider marker (use oraxen:/nexo:/craftengine:)", k);
                    return defaultBlockData;
                }

                String materialName = k.startsWith("minecraft:") ? k.substring("minecraft:".length()) : k;
                return Material.valueOf(materialName.toUpperCase()).createBlockData();
            } catch (IllegalArgumentException e) {
                log.error("'{}' is not a valid block (Minecraft / Oraxen / Nexo / CraftEngine)", k, e);
                return defaultBlockData;
            }
        });
    }
}
