package fr.euphyllia.skyllia.api.world;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.World;

/**
 * Ce code provient d'ici : <a href="https://github.com/Folia-Inquisitors/MoreFoWorld/blob/master/src/main/java/me/hsgamer/morefoworld/WorldUtil.java">MoreFoWorld</a> et CraftBukkit
 */
public final class WorldFeedback {

    private static final Logger logger = LogManager.getLogger(WorldFeedback.class);

    public enum Feedback {
        WORLD_ALREADY_EXISTS,
        WORLD_DUPLICATED,
        WORLD_FOLDER_INVALID,
        WORLD_DEFAULT,
        SUCCESS;

        public FeedbackWorld toFeedbackWorld(World world) {
            return new FeedbackWorld(world, this);
        }

        public FeedbackWorld toFeedbackWorld() {
            return new FeedbackWorld(this);
        }
    }

    public static class FeedbackWorld {
        public final World world;
        public final Feedback feedback;

        public FeedbackWorld(World world, Feedback feedback) {
            this.world = world;
            this.feedback = feedback;
        }

        public FeedbackWorld(Feedback feedback) {
            this(null, feedback);
        }
    }
}