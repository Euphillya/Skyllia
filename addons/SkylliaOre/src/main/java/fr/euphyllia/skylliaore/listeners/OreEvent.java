package fr.euphyllia.skylliaore.listeners;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliaore.Main;
import fr.euphyllia.skylliaore.api.Generator;
import io.th0rgal.oraxen.api.OraxenBlocks;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Random;

public class OreEvent implements Listener {

    private static final Logger log = LoggerFactory.getLogger(OreEvent.class);

    @EventHandler
    public void onBlockForm(final BlockFormEvent event) {
        if (event.isCancelled()) return;

        Location location = event.getBlock().getLocation();
        World world = location.getWorld();

        if (!SkylliaAPI.isWorldSkyblock(world)) return;

        Island island = SkylliaAPI.getIslandByChunk(location.getChunk());
        if (island == null) return;

        handleBlockFormation(event, world, island);
    }

    private void handleBlockFormation(BlockFormEvent event, World world, Island island) {
        String worldName = world.getName();
        Material blockType = event.getNewState().getType();

        Generator generator = Main.getCache().getGeneratorIsland(island.getId());

        if (generator.worlds().contains(worldName)) {
            for (String replace : generator.replaceBlocks()) {
                if (replace.equalsIgnoreCase(blockType.name())) {
                    BlockData blockByChance = getBlockByChance(generator.blockChances());
                    event.getNewState().setBlockData(blockByChance);
                    break;
                }
            }
        }
    }

    private BlockData getBlockByChance(Map<String, Double> blockChances) {
        double totalChance = blockChances.values().stream().mapToDouble(i -> i).sum();
        double randomChance = new Random().nextDouble() * totalChance;

        double currentChance = 0;
        for (Map.Entry<String, Double> entry : blockChances.entrySet()) {
            currentChance += entry.getValue();
            if (randomChance < currentChance) {
                try {
                    if (entry.getKey().startsWith("oraxen:") && Main.isOraxenLoaded()) {
                        String oraxenBlock = entry.getKey().split(":")[1];
                        BlockData data = OraxenBlocks.getOraxenBlockData(oraxenBlock);
                        if (data == null) continue;
                        return data;
                    } else {
                        return Material.valueOf(entry.getKey().toUpperCase()).createBlockData();
                    }
                } catch (Exception exception) {
                    log.error("{} is not a block Minecraft or Oraxen", entry.getKey());
                    return Material.COBBLESTONE.createBlockData();
                }
            }
        }
        return Material.COBBLESTONE.createBlockData(); // Default block if no match is found
    }
}
