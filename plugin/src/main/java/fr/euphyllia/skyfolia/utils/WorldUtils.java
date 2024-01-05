package fr.euphyllia.skyfolia.utils;

import fr.euphyllia.skyfolia.api.world.WorldFeedback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;

/**
 * Ce code provient d'ici : <a href="https://github.com/Folia-Inquisitors/MoreFoWorld/blob/master/src/main/java/me/hsgamer/morefoworld/WorldUtil.java">MoreFoWorld</a> et CraftBukkit
 */
public final class WorldUtils {

    private static final Logger logger = LogManager.getLogger(WorldUtils.class);

    public static WorldFeedback.FeedbackWorld addWorld(WorldCreator creator) {
        final String versionMC = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        return switch (versionMC) {
            case "v1_20_R2" -> fr.euphyllia.skyfolia.utils.nms.v1_20_R2.WorldNMS.createWorld(creator);
            default -> throw new RuntimeException("Pas pris en charge");
        };

    }
}