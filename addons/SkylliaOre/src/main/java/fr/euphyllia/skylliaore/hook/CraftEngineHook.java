package fr.euphyllia.skylliaore.hook;

import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CraftEngineHook {

    private static final Logger logger = LoggerFactory.getLogger(CraftEngineHook.class);

    private CraftEngineHook() {
    }

    @Nullable
    public static BlockData getBlockData(String blockId) {
        try {
            BlockDefinition definition = CraftEngineBlocks.byId(Key.of(blockId));
            if (definition == null) {
                logger.debug("CraftEngine block not found: {}", blockId);
                return null;
            }

            ImmutableBlockState state = definition.defaultState();
            return CraftEngineBlocks.getBukkitBlockData(state);
        } catch (Exception e) {
            logger.error("Failed to resolve CraftEngine block data for '{}'", blockId, e);
            return null;
        }
    }

    public static boolean isAvailable() {
        try {
            return CraftEngineBlocks.class.getName() != null;
        } catch (Throwable t) {
            logger.debug("CraftEngine API not available on the classpath", t);
            return false;
        }
    }
}