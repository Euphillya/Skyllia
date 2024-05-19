package fr.euphyllia.skyllia.utils.nms.v1_20_R4;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.world.WorldFeedback;
import net.kyori.adventure.util.TriState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.validation.ContentValidationException;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.generator.CraftWorldInfo;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

public class WorldNMS extends fr.euphyllia.skyllia.api.utils.nms.WorldNMS {

    static GameType getGameType(GameMode gameMode) {
        return switch (gameMode) {
            case SURVIVAL -> GameType.SURVIVAL;
            case CREATIVE -> GameType.CREATIVE;
            case ADVENTURE -> GameType.ADVENTURE;
            case SPECTATOR -> GameType.SPECTATOR;
        };
    }

    @Override
    public WorldFeedback.FeedbackWorld createWorld(WorldCreator creator) {
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        DedicatedServer console = craftServer.getServer();
        Preconditions.checkState(console.getAllLevels().iterator().hasNext(), "Cannot create additional worlds on STARTUP");
        //Preconditions.checkState(!console.isIteratingOverLevels, "Cannot create a world while worlds are being ticked"); // Paper - Cat - Temp disable. We'll see how this goes.
        Preconditions.checkArgument(creator != null, "WorldCreator cannot be null");

        String name = creator.name();
        ChunkGenerator generator = creator.generator();
        BiomeProvider biomeProvider = creator.biomeProvider();
        File folder = new File(craftServer.getWorldContainer(), name);
        World world = craftServer.getWorld(name);

        // Paper start
        World worldByKey = craftServer.getWorld(creator.key());
        if (world != null || worldByKey != null) {
            if (world != worldByKey) {
                return WorldFeedback.Feedback.WORLD_DUPLICATED.toFeedbackWorld();
            }
        }

        if ((folder.exists()) && (!folder.isDirectory())) {
            return WorldFeedback.Feedback.WORLD_FOLDER_INVALID.toFeedbackWorld();
        }

        if (generator == null) {
            generator = craftServer.getGenerator(name);
        }

        if (biomeProvider == null) {
            biomeProvider = craftServer.getBiomeProvider(name);
        }

        ResourceKey<LevelStem> actualDimension = switch (creator.environment()) {
            case NORMAL -> LevelStem.OVERWORLD;
            case NETHER -> LevelStem.NETHER;
            case THE_END -> LevelStem.END;
            default -> throw new IllegalArgumentException("Illegal dimension (" + creator.environment() + ")");
        };

        LevelStorageSource.LevelStorageAccess worldSession;
        try {
            worldSession = LevelStorageSource.createDefault(craftServer.getWorldContainer().toPath()).validateAndCreateAccess(name, actualDimension);
        } catch (IOException | ContentValidationException ex) {
            throw new RuntimeException(ex);
        }

        Dynamic<?> dynamic;
        if (worldSession.hasWorldData()) {
            net.minecraft.world.level.storage.LevelSummary worldinfo;

            try {
                dynamic = worldSession.getDataTag();
                worldinfo = worldSession.getSummary(dynamic);
            } catch (NbtException | ReportedNbtException | IOException ioexception) {
                LevelStorageSource.LevelDirectory convertable_b = worldSession.getLevelDirectory();

                MinecraftServer.LOGGER.warn("Failed to load world data from {}", convertable_b.dataFile(), ioexception);
                MinecraftServer.LOGGER.info("Attempting to use fallback");

                try {
                    dynamic = worldSession.getDataTagFallback();
                    worldinfo = worldSession.getSummary(dynamic);
                } catch (NbtException | ReportedNbtException | IOException ioexception1) {
                    MinecraftServer.LOGGER.error("Failed to load world data from {}", convertable_b.oldDataFile(), ioexception1);
                    MinecraftServer.LOGGER.error("Failed to load world data from {} and {}. World files may be corrupted. Shutting down.", convertable_b.dataFile(), convertable_b.oldDataFile());
                    return null;
                }

                worldSession.restoreLevelDataFromOld();
            }

            if (worldinfo.requiresManualConversion()) {
                MinecraftServer.LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                return null;
            }

            if (!worldinfo.isCompatible()) {
                MinecraftServer.LOGGER.info("This world was created by an incompatible version.");
                return null;
            }
        } else {
            dynamic = null;
        }

        boolean hardcore = creator.hardcore();

        PrimaryLevelData worlddata;
        WorldLoader.DataLoadContext worldloader_a = console.worldLoader;
        RegistryAccess.Frozen iregistrycustom_dimension = worldloader_a.datapackDimensions();
        net.minecraft.core.Registry<LevelStem> iregistry = iregistrycustom_dimension.registryOrThrow(Registries.LEVEL_STEM);
        if (dynamic != null) {
            LevelDataAndDimensions leveldataanddimensions = LevelStorageSource.getLevelDataAndDimensions(dynamic, worldloader_a.dataConfiguration(), iregistry, worldloader_a.datapackWorldgen());

            worlddata = (PrimaryLevelData) leveldataanddimensions.worldData();
            iregistry = leveldataanddimensions.dimensions().dimensions();
            iregistrycustom_dimension = leveldataanddimensions.dimensions().dimensionsRegistryAccess();
        } else {
            LevelSettings worldsettings;
            WorldOptions worldoptions = new WorldOptions(creator.seed(), creator.generateStructures(), false);
            WorldDimensions worlddimensions;

            DedicatedServerProperties.WorldDimensionData properties = new DedicatedServerProperties.WorldDimensionData(GsonHelper.parse((creator.generatorSettings().isEmpty()) ? "{}" : creator.generatorSettings()), creator.type().name().toLowerCase(Locale.ROOT));

            worldsettings = new LevelSettings(name, getGameType(GameMode.SURVIVAL), hardcore, Difficulty.EASY, false, new GameRules(), worldloader_a.dataConfiguration());
            worlddimensions = properties.create(worldloader_a.datapackWorldgen());

            WorldDimensions.Complete worlddimensions_b = worlddimensions.bake(iregistry);
            Lifecycle lifecycle = worlddimensions_b.lifecycle().add(worldloader_a.datapackWorldgen().allRegistriesLifecycle());

            worlddata = new PrimaryLevelData(worldsettings, worldoptions, worlddimensions_b.specialWorldProperty(), lifecycle);
            iregistry = worlddimensions_b.dimensions();
            iregistrycustom_dimension = worlddimensions_b.dimensionsRegistryAccess();
        }
        worlddata.customDimensions = iregistry;
        worlddata.checkName(name);
        worlddata.setModdedInfo(console.getServerModName(), console.getModdedStatus().shouldReportAsModified());

        // Paper - fix and optimise world upgrading; move down

        long j = BiomeManager.obfuscateSeed(worlddata.worldGenOptions().seed()); // Paper - use world seed
        List<CustomSpawner> list = ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(worlddata));
        LevelStem worlddimension = iregistry.get(actualDimension);

        WorldInfo worldInfo = new CraftWorldInfo(worlddata, worldSession, creator.environment(), worlddimension.type().value(), worlddimension.generator(), craftServer.getHandle().getServer().registryAccess()); // Paper - Expose vanilla BiomeProvider from WorldInfo
        if (biomeProvider == null && generator != null) {
            biomeProvider = generator.getDefaultBiomeProvider(worldInfo);
        }

        // Paper start - fix and optimise world upgrading
        if (console.options.has("forceUpgrade")) {
            net.minecraft.server.Main.convertWorldButItWorks(actualDimension, worldSession, DataFixers.getDataFixer(), iregistrycustom_dimension, worlddimension.generator().getTypeNameForDataFixer(), console.options.has("eraseCache"), console.options.has("recreateRegionFiles"));
        }
        // Paper end - fix and optimise world upgrading
        ResourceKey<net.minecraft.world.level.Level> worldKey;
        String levelName = craftServer.getServer().getProperties().levelName;
        if (name.equals(levelName + "_nether")) {
            worldKey = net.minecraft.world.level.Level.NETHER;
        } else if (name.equals(levelName + "_the_end")) {
            worldKey = net.minecraft.world.level.Level.END;
        } else {
            worldKey = ResourceKey.create(Registries.DIMENSION, new net.minecraft.resources.ResourceLocation(creator.key().getNamespace().toLowerCase(java.util.Locale.ENGLISH), creator.key().getKey().toLowerCase(java.util.Locale.ENGLISH))); // Paper
        }

        ServerLevel internal = new ServerLevel(console, console.executor, worldSession, worlddata, worldKey, worlddimension, craftServer.getServer().progressListenerFactory.create(11),
                worlddata.isDebugWorld(), j, creator.environment() == World.Environment.NORMAL ? list : ImmutableList.of(), true, console.overworld().getRandomSequences(), creator.environment(), generator, biomeProvider);

        try {
            setRandomSpawnSelection(internal);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        console.addLevel(internal);

        internal.setSpawnSettings(true, true);

        if (creator.keepSpawnLoaded() == TriState.FALSE) {
            worlddata.getGameRules().getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(0, null);
        }

        try {
            Class<?> regionizedServerClass = Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            Method getInstanceMethod = regionizedServerClass.getDeclaredMethod("getInstance");
            getInstanceMethod.setAccessible(true);
            Object regionizedServerInstance = getInstanceMethod.invoke(null);
            Method addWorldMethod = regionizedServerClass.getDeclaredMethod("addWorld", ServerLevel.class);
            addWorldMethod.setAccessible(true);
            addWorldMethod.invoke(regionizedServerInstance, internal);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        Bukkit.getPluginManager().callEvent(new WorldLoadEvent(internal.getWorld()));
        return WorldFeedback.Feedback.SUCCESS.toFeedbackWorld(internal.getWorld());
    }

    @Override
    public void resetChunk(World craftWorld, Position position) {
        final ServerLevel serverLevel = ((CraftWorld) craftWorld).getHandle();
        io.papermc.paper.util.TickThread.ensureTickThread(serverLevel, position.x(), position.z(), "Cannot regenerate chunk asynchronously");
        final net.minecraft.server.level.ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
        final ChunkPos chunkPos = new ChunkPos(position.x(), position.z());
        final net.minecraft.world.level.chunk.LevelChunk levelChunk = serverChunkCache.getChunk(chunkPos.x, chunkPos.z, true);
        final Iterable<BlockPos> blockPosIterable = BlockPos.betweenClosed(chunkPos.getMinBlockX(), serverLevel.getMinBuildHeight(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), serverLevel.getMaxBuildHeight() - 1, chunkPos.getMaxBlockZ());
        for (Entity entity : serverLevel.getChunkEntities(position.x(), position.z())) {
            if (entity instanceof Player) continue;
            entity.remove();
        }
        for (final BlockPos blockPos : blockPosIterable) {
            levelChunk.removeBlockEntity(blockPos);
            serverLevel.setBlock(blockPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 16);
        }
        for (final BlockPos blockPos : blockPosIterable) { // Fix memory issue client
            serverChunkCache.blockChanged(blockPos);
        }
    }

    private static void setRandomSpawnSelection(ServerLevel serverLevel) throws NoSuchFieldException, IllegalAccessException {
        Class<?> clazz = serverLevel.getClass();

        // Obtention du champ 'randomSpawnSelection'
        Field randomSpawnSelectionField = clazz.getDeclaredField("randomSpawnSelection");
        randomSpawnSelectionField.setAccessible(true);

        ChunkPos newValue = new ChunkPos(serverLevel.getChunkSource().randomState().sampler().findSpawnPosition());
        randomSpawnSelectionField.set(serverLevel, newValue);
    }
}
