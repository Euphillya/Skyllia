package fr.euphyllia.skyllia.api.world;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.World;

/**
 * This code originates from <a href="https://github.com/Folia-Inquisitors/MoreFoWorld/blob/master/src/main/java/me/hsgamer/morefoworld/WorldUtil.java">MoreFoWorld</a> and CraftBukkit.
 * The WorldFeedback class provides feedback on world-related operations.
 */
public final class WorldFeedback {

    private static final Logger logger = LogManager.getLogger(WorldFeedback.class);

    /**
     * The Feedback enum represents various feedback states for world-related operations.
     */
    public enum Feedback {
        WORLD_ALREADY_EXISTS,
        WORLD_DUPLICATED,
        WORLD_FOLDER_INVALID,
        WORLD_DEFAULT,
        SUCCESS;

        /**
         * Converts this feedback to a FeedbackWorld object with the specified world.
         *
         * @param world The world associated with the feedback.
         * @return A new FeedbackWorld object.
         */
        public FeedbackWorld toFeedbackWorld(World world) {
            return new FeedbackWorld(world, this);
        }

        /**
         * Converts this feedback to a FeedbackWorld object without a specific world.
         *
         * @return A new FeedbackWorld object.
         */
        public FeedbackWorld toFeedbackWorld() {
            return new FeedbackWorld(this);
        }
    }

    /**
     * The FeedbackWorld class associates a world with a specific feedback.
     */
    public static class FeedbackWorld {
        public final World world;
        public final Feedback feedback;

        /**
         * Constructs a new FeedbackWorld with the specified world and feedback.
         *
         * @param world    The world associated with the feedback.
         * @param feedback The feedback state.
         */
        public FeedbackWorld(World world, Feedback feedback) {
            this.world = world;
            this.feedback = feedback;
        }

        /**
         * Constructs a new FeedbackWorld with the specified feedback and no specific world.
         *
         * @param feedback The feedback state.
         */
        public FeedbackWorld(Feedback feedback) {
            this(null, feedback);
        }
    }
}
