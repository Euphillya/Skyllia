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
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.plugin.Plugin;

import java.util.Random;

public class WorldsManager {
    private final Logger logger;
    private final InterneAPI api;

    public WorldsManager(InterneAPI interneAPI) {
        this.api = interneAPI;
        this.logger = LogManager.getLogger(this);
    }

    public void initWorld() {

        ConfigLoader.worldManager.getWorldConfigs().forEach((name, worldConfig) -> {
            WorldCreator worldCreator = new WorldCreator(name);

            String generatorId = worldConfig.getGenerator();
            if (generatorId.equalsIgnoreCase("default")) {
                worldCreator.generator(new VoidWorldGen());
            } else {
                Plugin plugin = Bukkit.getPluginManager().getPlugin(generatorId);
                if (plugin == null) {
                    String message = String.format(
                            "[WorldInit] Failed to load world \"%s\": generator plugin \"%s\" not found. " +
                                    "Please ensure the plugin providing this generator is installed.",
                            name, generatorId
                    );
                    throw new IllegalArgumentException(message);
                }
            }

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
