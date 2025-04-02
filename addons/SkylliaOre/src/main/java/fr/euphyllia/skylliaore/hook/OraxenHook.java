package fr.euphyllia.skylliaore.hook;

import io.th0rgal.oraxen.api.OraxenBlocks;
import org.bukkit.block.data.BlockData;

public class OraxenHook {

    public static BlockData getBlockData(String id) {
        return OraxenBlocks.getOraxenBlockData(id);
    }
}
