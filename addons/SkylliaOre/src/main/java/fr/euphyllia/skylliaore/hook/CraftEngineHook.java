package fr.euphyllia.skylliaore.hook;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
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
            BukkitBlockManager blockManager = BukkitBlockManager.instance();
            if (blockManager == null) {
                return null;
            }

            // Parse block ID and properties
            String blockId;
            String properties = null;
            
            int bracketIndex = id.indexOf('[');
            if (bracketIndex > 0) {
                blockId = id.substring(0, bracketIndex);
                properties = id.substring(bracketIndex);
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
            
            // Get default state
            ImmutableBlockState blockState = blockDef.variantProvider().defaultState();
            
            // If properties are specified, try to parse them
            if (properties != null && !properties.isEmpty()) {
                // Try to create using the full state string
                var fullState = blockManager.createBlockState(id);
                if (fullState != null) {
                    Object minecraftState = fullState.minecraftState();
                    return BlockStateUtils.fromBlockData(minecraftState);
                }
            }
            
            // Return BlockData of the default state
            if (blockState != null && blockState.visualBlockState() != null) {
                Object minecraftState = blockState.visualBlockState().minecraftState();
                return BlockStateUtils.fromBlockData(minecraftState);
            }
            
            return null;
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
            BukkitBlockManager blockManager = BukkitBlockManager.instance();
            return blockManager != null;
        } catch (Throwable e) {
            return false;
        }
    }
}
