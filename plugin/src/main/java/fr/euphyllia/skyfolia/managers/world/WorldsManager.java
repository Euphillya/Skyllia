package fr.euphyllia.skyfolia.managers.world;

import fr.euphyllia.skyfolia.api.InterneAPI;
import fr.euphyllia.skyfolia.api.exceptions.UnsupportedMinecraftVersionException;
import fr.euphyllia.skyfolia.api.world.WorldFeedback;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import fr.euphyllia.skyfolia.configuration.section.WorldConfig;
import fr.euphyllia.skyfolia.utils.WorldUtils;
import fr.euphyllia.skyfolia.utils.generators.VoidWorldGen;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class WorldsManager {
    private final Logger logger;
    private final InterneAPI api;

    public WorldsManager(InterneAPI interneAPI) {
        this.api = interneAPI;
        this.logger = LogManager.getLogger("fr.euphyllia.skyfolia.managers.world.WorldsManager");
    }

    public void initWorld() {
        ChunkGenerator chunkGenerator = new VoidWorldGen();
        for (WorldConfig worldConfig : ConfigToml.worldConfigs) {
            WorldCreator worldCreator = new WorldCreator(worldConfig.name());
            worldCreator.generator(chunkGenerator);
            worldCreator.type(WorldType.FLAT);
            worldCreator.seed(new Random(System.currentTimeMillis()).nextLong());
            worldCreator.environment(World.Environment.valueOf(worldConfig.environment().toUpperCase()));
            World w;
            try {
                w = worldCreator.createWorld(); // Work with Paper, not Folia
            } catch (Exception ignored) {
                WorldFeedback.FeedbackWorld feedbackWorld = null;
                try {
                    feedbackWorld = WorldUtils.addWorld(worldCreator);
                } catch (UnsupportedMinecraftVersionException e) {
                    logger.log(Level.FATAL, e.getMessage(), e);
                    return;
                }
                if (feedbackWorld.feedback == WorldFeedback.Feedback.SUCCESS) {
                    w = feedbackWorld.world;
                } else {
                    logger.log(Level.FATAL, "WORLD IMPOSSIBLE TO CREATE");
                    return;
                }

            }
            if (w != null) {
                w.setAutoSave(true);
                w.setSpawnLocation(0, 62, 0);
            }
        }
    }
}
