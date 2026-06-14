package fr.euphyllia.skylliaore.hook;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.UpdateFlags;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CraftEngineHook {

    private static final Logger logger = LoggerFactory.getLogger(CraftEngineHook.class);

    /**
     * Get BlockStateWrapper from CraftEngine block ID
     * Supports formats: "namespace:block_id" or "namespace:block_id[properties]"
     * 
     * @param id CraftEngine block identifier, e.g. "craftengine:custom_ore" or "craftengine:custom_ore[variant=iron]"
     * @return The corresponding BlockStateWrapper, or null if the block doesn't exist
     */
    @Nullable
    public static BlockStateWrapper getBlockStateWrapper(String id) {
        try {
            // Parse block ID (ignore properties for now, just get the base block)
            String blockId;
            
            int bracketIndex = id.indexOf('[');
            if (bracketIndex > 0) {
                blockId = id.substring(0, bracketIndex);
            } else {
                blockId = id;
            }

            // Get block definition using CraftEngine API
            Key key = Key.of(blockId);
            BlockDefinition blockDef = CraftEngineBlocks.byId(key);
            
            if (blockDef == null) {
                logger.debug("CraftEngine block not found: {}", blockId);
                return null;
            }

            // Get default state
            ImmutableBlockState blockState = blockDef.defaultState();
            
            // Convert to BlockStateWrapper
            BlockStateWrapper wrapper = blockState.customBlockState();
            
            logger.debug("Successfully resolved CraftEngine block: {} -> {}", blockId, wrapper);
            return wrapper;
        } catch (Exception e) {
            logger.error("Error getting BlockStateWrapper for CraftEngine block: {}", id, e);
            return null;
        }
    }

    /**
     * Place a CraftEngine block at the specified location
     * 
     * @param location The location where to place the block
     * @param id CraftEngine block identifier, e.g. "craftengine:custom_ore"
     * @return true if the block was successfully placed, false otherwise
     */
    public static boolean placeBlock(Location location, String id) {
        try {
            BlockStateWrapper wrapper = getBlockStateWrapper(id);
            if (wrapper == null) {
                return false;
            }

            // Get CraftEngine world wrapper using BukkitAdaptor
            BukkitWorld ceWorld = BukkitAdaptor.adapt(location.getWorld());
            if (ceWorld == null) {
                logger.error("Failed to adapt Bukkit world to CraftEngine world");
                return false;
            }

            // Place the block using CraftEngine API with UPDATE_ALL flag
            ceWorld.setBlockState(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                wrapper,
                UpdateFlags.UPDATE_ALL
            );

            logger.debug("Successfully placed CraftEngine block {} at {}", id, location);
            return true;
        } catch (Exception e) {
            logger.error("Error placing CraftEngine block {} at {}", id, location, e);
            return false;
        }
    }

    /**
     * Check if CraftEngine is loaded and the block manager is available
     * 
     * @return true if CraftEngine is available, false otherwise
     */
    public static boolean isAvailable() {
        try {
            // Try to access CraftEngineBlocks API to verify it's loaded
            CraftEngineBlocks.loadedBlocks();
            return true;
        } catch (Throwable e) {
            logger.warn("CraftEngine is not available", e);
            return false;
        }
    }
}
