package fr.euphyllia.skylliaore.listeners;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliaore.SkylliaOre;
import fr.euphyllia.skylliaore.api.Generator;
import fr.euphyllia.skylliaore.hook.NexoHook;
import fr.euphyllia.skylliaore.hook.OraxenHook;
import fr.euphyllia.skylliaore.utils.OptimizedGenerator;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class OreEvent implements Listener {

    private static final Logger log = LoggerFactory.getLogger(OreEvent.class);
    private static final boolean isOraxenLoaded = SkylliaOre.isOraxenLoaded();
    private static final boolean isNexoLoaded = SkylliaOre.isNexoLoaded();
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

        Generator generator = getGeneratorSync(island.getId());
        if (generator == null) return;


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

    private Generator getGeneratorSync(UUID islandId) {
        return SkylliaOre.getInstance().getOreGenerator().getGenIsland(islandId).getNow(SkylliaOre.getDefaultConfig().getDefaultGenerator());
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

        return Material.COBBLESTONE.createBlockData();
    }

    private BlockData getCachedBlockData(String key) {
        return blockDataCache.computeIfAbsent(key, k -> {
            try {
                if (k.startsWith("oraxen:") && isOraxenLoaded) {
                    String oraxenBlock = k.substring("oraxen:".length());
                    BlockData data = OraxenHook.getBlockData(oraxenBlock);
                    if (data != null) return data;
                } else if (k.startsWith("nexo:") && isNexoLoaded) {
                    String nexoBlock = k.substring("nexo:".length());
                    BlockData data = NexoHook.getBlockData(nexoBlock);
                    if (data != null) return data;
                }
                return Material.valueOf(k.toUpperCase()).createBlockData();
            } catch (Exception e) {
                log.error("{} is not a valid block in Minecraft, Oraxen or Nexo", k, e);
                return Material.COBBLESTONE.createBlockData();
            }
        });
    }
}
