package fr.euphyllia.skylliaore.listeners;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliaore.Main;
import fr.euphyllia.skylliaore.api.Generator;
import fr.euphyllia.skylliaore.utils.OptimizedGenerator;
import io.th0rgal.oraxen.api.OraxenBlocks;
import org.bukkit.Material;
import org.bukkit.World;
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
    private static final boolean isOraxenLoaded = Main.isOraxenLoaded();
    private static final ConcurrentHashMap<String, BlockData> blockDataCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, OptimizedGenerator> optimizedGeneratorCache = new ConcurrentHashMap<>();

    @EventHandler
    public void onBlockForm(final BlockFormEvent event) {
        if (event.isCancelled()) return;

        World world = event.getBlock().getWorld();

        if (!SkylliaAPI.isWorldSkyblock(world)) return;

        Island island = SkylliaAPI.getIslandByChunk(event.getBlock().getChunk());
        if (island == null) return;

        handleBlockFormation(event, world, island);
    }

    private void handleBlockFormation(BlockFormEvent event, World world, Island island) {
        String worldName = world.getName().toLowerCase();
        Material blockType = event.getNewState().getType();

        Generator generator = Main.getCache().getGeneratorIsland(island.getId());
        if (generator == null) return;

        // Utiliser un cache pour OptimizedGenerator
        OptimizedGenerator optimizedGenerator = optimizedGeneratorCache.computeIfAbsent(generator.name(),
                name -> new OptimizedGenerator(generator));

        if (optimizedGenerator.getGenerator().worlds().contains(worldName)) {
            String blockName = blockType.name().toLowerCase();
            if (optimizedGenerator.getGenerator().replaceBlocks().contains(blockName)) {
                BlockData blockByChance = getBlockByChance(optimizedGenerator);
                event.getNewState().setBlockData(blockByChance);
            }
        }
    }

    private BlockData getBlockByChance(OptimizedGenerator optimizedGenerator) {
        double randomChance = ThreadLocalRandom.current().nextDouble() * optimizedGenerator.getTotalChance();
        List<OptimizedGenerator.BlockProbability> cumulativeProbabilities = optimizedGenerator.getCumulativeProbabilities();

        // Recherche binaire
        int index = Collections.binarySearch(cumulativeProbabilities, new OptimizedGenerator.BlockProbability("", randomChance),
                Comparator.comparingDouble(OptimizedGenerator.BlockProbability::cumulativeChance));

        if (index < 0) {
            index = -index - 1;
        }

        if (index >= 0 && index < cumulativeProbabilities.size()) {
            return getCachedBlockData(cumulativeProbabilities.get(index).blockKey());
        }

        return Material.COBBLESTONE.createBlockData(); // Bloc par défaut
    }

    private BlockData getCachedBlockData(String key) {
        return blockDataCache.computeIfAbsent(key, k -> {
            try {
                if (k.startsWith("oraxen:") && isOraxenLoaded) {
                    String oraxenBlock = k.substring("oraxen:".length());
                    BlockData data = OraxenBlocks.getOraxenBlockData(oraxenBlock);
                    if (data != null) return data;
                } else {
                    return Material.valueOf(k.toUpperCase()).createBlockData();
                }
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("{} is not a valid block in Minecraft or Oraxen", k);
                }
            }
            return Material.COBBLESTONE.createBlockData();
        });
    }
}
