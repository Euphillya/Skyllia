package fr.euphyllia.skyllia.managers.world;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.world.WorldFeedback;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.utils.WorldUtils;
import fr.euphyllia.skyllia.utils.generators.VoidWorldGen;
import net.kyori.adventure.util.TriState;
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
        this.logger = LogManager.getLogger(this);
    }

    public void initWorld() {
        ChunkGenerator chunkGenerator = new VoidWorldGen();
        ConfigLoader.worldManager.getWorldConfigs().forEach((name, worldConfig) -> {
            WorldCreator worldCreator = new WorldCreator(name);
            worldCreator.generator(chunkGenerator);
            worldCreator.type(WorldType.FLAT);
            worldCreator.seed(new Random(System.currentTimeMillis()).nextLong());
            worldCreator.environment(worldConfig.getEnvironment());
            World w;
            try {
                w = worldCreator.createWorld(); // Work with Paper, not Folia
            } catch (Exception ignored) {
                worldCreator.keepSpawnLoaded(TriState.TRUE); // Toujours chargé le monde ! Prévenir du crash avec le PlayerRespawnLogic
                WorldFeedback.FeedbackWorld feedbackWorld = WorldUtils.addWorld(this.api, worldCreator);
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
        });
    }
}
