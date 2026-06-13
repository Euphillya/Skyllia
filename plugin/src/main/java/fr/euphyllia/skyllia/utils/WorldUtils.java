package fr.euphyllia.skyllia.utils;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.world.WorldFeedback;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Ce code provient d'ici : <a href="https://github.com/Folia-Inquisitors/MoreFoWorld/blob/master/src/Skyllia/java/me/hsgamer/morefoworld/WorldUtil.java">MoreFoWorld</a> et CraftBukkit
 */
public final class WorldUtils {

    private static final Logger logger = LogManager.getLogger(WorldUtils.class);

    public static WorldFeedback.FeedbackWorld addWorld(InterneAPI interneAPI, WorldCreator creator) {
        return interneAPI.getWorldNMS().createWorld(creator);
    }

    public static WorldFeedback.FeedbackWorld addWorld(InterneAPI interneAPI, WorldCreator creator, WorldConfig worldConfig) {
        return interneAPI.getWorldNMS().createWorld(creator, worldConfig);
    }

    public static Boolean isWorldSkyblock(String name) {
        Map<String, WorldConfig> configs = ConfigLoader.worldManager.getWorldConfigs();
        return configs.containsKey(name);
    }

    public static List<WorldConfig> getWorldConfigs() {
        return new ArrayList<>(ConfigLoader.worldManager.getWorldConfigs().values());
    }

    public static @Nullable WorldConfig getWorldConfig(String worldName) {
        return ConfigLoader.worldManager.getWorldConfig(worldName);
    }

    /**
     * Checks if a location is safe (solid ground with 2 breathable blocks)
     *
     * @param location Location to check
     * @return True if location is safe
     */
    public static boolean isSafeLocation(Location location) {
        Block feet = location.getBlock();
        Block head = feet.getRelative(BlockFace.UP);
        Block ground = feet.getRelative(BlockFace.DOWN);

        if (!feet.isPassable() || !head.isPassable()) {
            return false;
        }

        if (isDangerous(feet) || isDangerous(head)) {
            return false;
        }

        if (!ground.getType().isSolid()) {
            return false;
        }

        if (isDangerous(ground)) {
            return false;
        }

        return true;
    }

    private static boolean isDangerous(Block block) {
        return switch (block.getType()) {
            case LAVA, FIRE, SOUL_FIRE, MAGMA_BLOCK,
                 SWEET_BERRY_BUSH, WITHER_ROSE,
                 CACTUS, POWDER_SNOW,
                 NETHER_PORTAL, END_PORTAL, END_GATEWAY -> true;
            default -> false;
        };
    }
}
