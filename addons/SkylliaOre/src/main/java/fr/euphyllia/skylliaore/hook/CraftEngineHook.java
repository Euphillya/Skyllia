package fr.euphyllia.skylliaore.hook;

import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.BlockManager;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CraftEngineHook {

    /**
     * Get BlockData from CraftEngine block ID
     * Supports formats: "namespace:block_id" or "namespace:block_id[properties]"
     * 
     * @param id CraftEngine block identifier, e.g. "craftengine:custom_ore" or "craftengine:custom_ore[variant=iron]"
     * @return The corresponding BlockData, or null if the block doesn't exist
     */
    @Nullable
    public static BlockData getBlockData(String id) {
        try {
            // Get the BlockManager instance from CraftEngine
            CraftEngine craftEngine = CraftEngine.instance();
            if (craftEngine == null) {
                return null;
            }
            
            BlockManager blockManager = craftEngine.blockManager();
            if (blockManager == null) {
                return null;
            }

            // Parse block ID (ignore properties for now, just get the base block)
            String blockId;
            
            int bracketIndex = id.indexOf('[');
            if (bracketIndex > 0) {
                blockId = id.substring(0, bracketIndex);
            } else {
                blockId = id;
            }

            // Get block definition
            Key key = Key.of(blockId);
            Optional<BlockDefinition> blockDefOpt = blockManager.blockById(key);
            
            if (blockDefOpt.isEmpty()) {
                return null;
            }

            BlockDefinition blockDef = blockDefOpt.get();
            
            // Get default state and convert to BlockData
            // The visual block state is what the client sees
            Object minecraftState = blockDef.defaultState().visualBlockState().minecraftState();
            
            // Use BlockStateUtils to convert to BlockData
            return BlockStateUtils.fromBlockData(minecraftState);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check if CraftEngine is loaded and the block manager is available
     * 
     * @return true if CraftEngine is available, false otherwise
     */
    public static boolean isAvailable() {
        try {
            CraftEngine craftEngine = CraftEngine.instance();
            if (craftEngine == null) {
                return false;
            }
            BlockManager blockManager = craftEngine.blockManager();
            return blockManager != null;
        } catch (Throwable e) {
            return false;
        }
    }
}
