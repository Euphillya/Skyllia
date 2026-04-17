package fr.euphyllia.skyllia.managers.world;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.world.GeneratorFactory;
import fr.euphyllia.skyllia.api.world.WorldFeedback;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.utils.WorldUtils;
import fr.euphyllia.skyllia.utils.generators.FixedBiomeProvider;
import fr.euphyllia.skyllia.utils.generators.OceanWorldGen;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class WorldsManager {

    private final Logger logger;
    private final InterneAPI api;
    private final Map<String, GeneratorFactory> generators = new HashMap<>();

    public WorldsManager(InterneAPI interneAPI) {
        this.api = interneAPI;
        this.logger = LogManager.getLogger(this);
        registerGenerators();
    }

    private void registerGenerators() {
        generators.put("default", (name, config) -> new VoidWorldGen());
        generators.put("ocean", (name, config) -> {
            Integer seaHeight = config.getSeaHeight();
            String seaBlockStr = config.getSeaBlock();

            if (seaHeight == null) {
                throw new IllegalArgumentException(
                        String.format("Invalid sea height for world \"%s\": seaHeight must be defined for ocean generator.", name)
                );
            }
            if (seaBlockStr == null) {
                throw new IllegalArgumentException(
                        String.format("Invalid sea block for world \"%s\": seaBlock must be defined for ocean generator.", name)
                );
            }

            return new OceanWorldGen(seaHeight, OceanWorldGen.parseMaterial(seaBlockStr));
        });
    }

    public void registerGenerator(String id, GeneratorFactory factory) {
        generators.put(id, factory);
    }

    public void initWorld() {

        for (Map.Entry<String, WorldConfig> entry : ConfigLoader.worldManager.getWorldConfigs().entrySet()) {
            String name = entry.getKey();
            WorldConfig worldConfig = entry.getValue();

            if (Bukkit.getWorld(name) != null) {
                continue;
            }

            WorldCreator worldCreator = new WorldCreator(name);

            try {
                applyGenerator(name, worldConfig, worldCreator);
            } catch (IllegalArgumentException e) {
                logger.log(Level.FATAL, e.getMessage());
                continue;
            }

            worldCreator.type(WorldType.FLAT);
            worldCreator.seed(new Random(System.currentTimeMillis()).nextLong());
            worldCreator.environment(worldConfig.getEnvironment());

            worldCreator.biomeProvider(
                    FixedBiomeProvider.fromConfig(
                            worldConfig.getEnvironment(),
                            this.api.getBiomesImpl(),
                            worldConfig.getBiomeId()
                    )
            );

            worldCreator.keepSpawnLoaded(TriState.TRUE); // Toujours chargé le monde ! Prévenir du crash avec le PlayerRespawnLogic

            World w;
            WorldFeedback.FeedbackWorld feedbackWorld = WorldUtils.addWorld(this.api, worldCreator, worldConfig);
            if (feedbackWorld.feedback == WorldFeedback.Feedback.SUCCESS) {
                w = feedbackWorld.world;
            } else {
                logger.log(Level.FATAL, "WORLD IMPOSSIBLE TO CREATE");
                continue;
            }
            if (w != null) {
                w.setAutoSave(true);
                w.setSpawnLocation(0, 62, 0);
            }
        }
    }

    private void applyGenerator(String name, WorldConfig config, WorldCreator worldCreator) {
        String generatorId = config.getGenerator();
        GeneratorFactory factory = generators.get(generatorId);
        if (factory != null) {
            worldCreator.generator(factory.create(name, config));
        } else {
            String pluginName = generatorId.contains(":")
                    ? generatorId.split(":")[0]
                    : generatorId;

            Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
            if (plugin == null) {
                throw new IllegalArgumentException(
                        String.format(
                                "[WorldInit] Failed to load world \"%s\": generator plugin \"%s\" not found. " +
                                        "Please ensure the plugin providing this generator is installed.",
                                name, pluginName
                        )
                );
            }
            worldCreator.generator(generatorId);
        }
    }
}
