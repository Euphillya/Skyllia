package fr.euphyllia.skyfolia.utils;

import fr.euphyllia.skyfolia.api.configuration.WorldConfig;
import fr.euphyllia.skyfolia.api.exceptions.UnsupportedMinecraftVersionException;
import fr.euphyllia.skyfolia.api.world.WorldFeedback;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Ce code provient d'ici : <a href="https://github.com/Folia-Inquisitors/MoreFoWorld/blob/master/src/main/java/me/hsgamer/morefoworld/WorldUtil.java">MoreFoWorld</a> et CraftBukkit
 */
public final class WorldUtils {

    private static final Logger logger = LogManager.getLogger(WorldUtils.class);

    public static WorldFeedback.FeedbackWorld addWorld(WorldCreator creator) throws UnsupportedMinecraftVersionException {
        final String versionMC = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        return switch (versionMC) {
            case "v1_19_R3" -> fr.euphyllia.skyfolia.utils.nms.v1_19_R3.WorldNMS.createWorld(creator);
            case "v1_20_R1" -> fr.euphyllia.skyfolia.utils.nms.v1_20_R1.WorldNMS.createWorld(creator);
            case "v1_20_R2" -> fr.euphyllia.skyfolia.utils.nms.v1_20_R2.WorldNMS.createWorld(creator);
            default ->
                    throw new UnsupportedMinecraftVersionException("Version %s not supported !".formatted(versionMC));
        };
    }

    public static Boolean isWorldSkyblock(String name) {
        return ConfigToml.worldConfigs.stream().anyMatch(worldConfig -> worldConfig.name().equalsIgnoreCase(name));
    }

    public static List<WorldConfig> getWorldConfigs() {
        return ConfigToml.worldConfigs;
    }

    public static @Nullable WorldConfig getWorldConfig(String worldName) {
        return ConfigToml.worldConfigs.stream().filter(worldConfig -> worldConfig.name().equalsIgnoreCase(worldName)).findFirst().orElse(null);
    }

    // https://www.spigotmc.org/threads/safely-teleport-players.83205/

    /**
     * Checks if a location is safe (solid ground with 2 breathable blocks)
     *
     * @param location Location to check
     * @return True if location is safe
     */
    public static boolean isSafeLocation(Location location) {
        Block feet = location.getBlock();
        if (!feet.getType().isTransparent() && !feet.getLocation().add(0, 1, 0).getBlock().getType().isTransparent()) {
            return false; // not transparent (will suffocate)
        }
        Block head = feet.getRelative(BlockFace.UP);
        if (!head.getType().isTransparent()) {
            return false; // not transparent (will suffocate)
        }
        Block ground = feet.getRelative(BlockFace.DOWN);
        return ground.getType().isSolid(); // not solid
    }
}