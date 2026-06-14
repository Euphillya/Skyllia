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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class OreEvent implements Listener {

    private static final Logger log = LoggerFactory.getLogger(OreEvent.class);
    private static final boolean isOraxenLoaded = SkylliaOre.isOraxenLoaded();
    private static final boolean isNexoLoaded = SkylliaOre.isNexoLoaded();
    private static final boolean isCraftEngineLoaded = SkylliaOre.isCraftEngineLoaded();
    private static final ConcurrentHashMap<String, BlockData> blockDataCache = new ConcurrentHashMap<>();

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
                String selectedBlockKey = getBlockKeyByChance(optimized);
                
                // Check if it's a CraftEngine block and handle it specially
                if ((selectedBlockKey.startsWith("craftengine:") || (selectedBlockKey.contains(":") && !selectedBlockKey.contains("minecraft:"))) && isCraftEngineLoaded) {
                    // For CraftEngine blocks, we allow the event to complete first, then replace the block
                    Location location = event.getBlock().getLocation();
                    
                    // Use Folia-compatible chunk scheduler
                    event.getBlock().getChunk().getPluginChunkTickScheduler(SkylliaOre.getInstance())
                        .runDelayed((task) -> {
                            CraftEngineHook.placeBlock(location, selectedBlockKey);
                        }, 1L);
                } else {
                    // For vanilla/Oraxen/Nexo blocks, use the old method
                    BlockData blockByChance = getCachedBlockData(selectedBlockKey);
                    event.getNewState().setBlockData(blockByChance);
                }
            }
        }
    }

    private Generator getGeneratorSync(UUID islandId) {
        return SkylliaOre.getCachedGenerator(islandId);
    }

    private String getBlockKeyByChance(OptimizedGenerator optimizedGenerator) {
        double randomChance = ThreadLocalRandom.current().nextDouble() * optimizedGenerator.getTotalChance();
        List<OptimizedGenerator.BlockProbability> cumulativeProbabilities = optimizedGenerator.getCumulativeProbabilities();

        int index = Collections.binarySearch(cumulativeProbabilities, new OptimizedGenerator.BlockProbability("", randomChance),
                Comparator.comparingDouble(OptimizedGenerator.BlockProbability::cumulativeChance));

        if (index < 0) {
            index = -index - 1;
        }

        if (index >= 0 && index < cumulativeProbabilities.size()) {
            return cumulativeProbabilities.get(index).blockKey();
        }

        return "cobblestone";
    }

    private BlockData getBlockByChance(OptimizedGenerator optimizedGenerator) {
        return getCachedBlockData(getBlockKeyByChance(optimizedGenerator));
    }

    private BlockData getCachedBlockData(String key) {
        return blockDataCache.computeIfAbsent(key, k -> {
            try {
                if (k.startsWith("oraxen:") && isOraxenLoaded) {
                    String oraxenBlock = k.substring("oraxen:".length());
                    BlockData data = OraxenHook.getBlockData(oraxenBlock);
                    if (data != null) return data;
                    log.error("{} is not a valid Oraxen block", k);
                    return Material.COBBLESTONE.createBlockData();
                } else if (k.startsWith("nexo:") && isNexoLoaded) {
                    String nexoBlock = k.substring("nexo:".length());
                    BlockData data = NexoHook.getBlockData(nexoBlock);
                    if (data != null) return data;
                    log.error("{} is not a valid Nexo block", k);
                    return Material.COBBLESTONE.createBlockData();
                } else if (k.contains(":") && !k.startsWith("minecraft:")) {
                    // For CraftEngine blocks, return a placeholder (note blocks are handled separately)
                    // This shouldn't be called for CraftEngine blocks anymore
                    log.warn("{} appears to be a custom block that should be handled specially", k);
                    return Material.NOTE_BLOCK.createBlockData();
                }
                // Try vanilla Minecraft material
                String materialName = k.startsWith("minecraft:") ? k.substring("minecraft:".length()) : k;
                return Material.valueOf(materialName.toUpperCase()).createBlockData();
            } catch (IllegalArgumentException e) {
                log.error("{} is not a valid Minecraft material", k);
                return Material.COBBLESTONE.createBlockData();
            } catch (Exception e) {
                log.error("Error processing block data for {}", k, e);
                return Material.COBBLESTONE.createBlockData();
            }
        });
    }
}
