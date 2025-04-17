package fr.euphyllia.skylliaore.hook;

import com.nexomc.nexo.api.NexoBlocks;
import org.bukkit.block.data.BlockData;

public class NexoHook {

    public static BlockData getBlockData(String id) {
        return NexoBlocks.blockData(id);
    }
}
