package fr.euphyllia.skyllia.utils.nms.v1_20_R1;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.world.WorldFeedback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.generator.CraftWorldInfo;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
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
        io.papermc.paper.threadedregions.RegionizedServer.ensureGlobalTickThread("World create can be done only on global tick thread");
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        DedicatedServer console = craftServer.getServer();

        String name = creator.name();

        String levelName = console.getProperties().levelName;
        if (name.equals(levelName)
                || (console.isNetherEnabled() && name.equals(levelName + "_nether"))
                || (craftServer.getAllowEnd() && name.equals(levelName + "_the_end"))
        ) {
            return WorldFeedback.Feedback.WORLD_DEFAULT.toFeedbackWorld();
        }

        ChunkGenerator generator = creator.generator();
        BiomeProvider biomeProvider = creator.biomeProvider();
        File folder = new File(craftServer.getWorldContainer(), name);
        org.bukkit.World world = craftServer.getWorld(name);

        CraftWorld worldByKey = (CraftWorld) craftServer.getWorld(creator.key());
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
            default -> throw new IllegalArgumentException("Illegal dimension");
        };

        LevelStorageSource.LevelStorageAccess worldSession;
        try {
            worldSession = LevelStorageSource.createDefault(craftServer.getWorldContainer().toPath()).createAccess(name, actualDimension);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        boolean hardcore = creator.hardcore();

        PrimaryLevelData worlddata;
        WorldLoader.DataLoadContext worldloader_a = console.worldLoader;
        net.minecraft.core.Registry<LevelStem> iregistry = worldloader_a.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM);
        DynamicOps<Tag> dynamicops = RegistryOps.create(NbtOps.INSTANCE, worldloader_a.datapackWorldgen());
        Pair<WorldData, WorldDimensions.Complete> pair = worldSession.getDataTag(dynamicops, worldloader_a.dataConfiguration(), iregistry, worldloader_a.datapackWorldgen().allRegistriesLifecycle());


        if (pair != null) {
            worlddata = (PrimaryLevelData) pair.getFirst();
            iregistry = pair.getSecond().dimensions();
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
        }
        worlddata.customDimensions = iregistry;
        worlddata.checkName(name);
        worlddata.setModdedInfo(console.getServerModName(), console.getModdedStatus().shouldReportAsModified());

        long j = BiomeManager.obfuscateSeed(creator.seed());
        List<CustomSpawner> list = ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(worlddata));
        LevelStem worlddimension = iregistry.get(actualDimension);

        WorldInfo worldInfo = new CraftWorldInfo(worlddata, worldSession, creator.environment(), worlddimension.type().value(), worlddimension.generator(), craftServer.getHandle().getServer().registryAccess()); // Paper
        if (biomeProvider == null && generator != null) {
            biomeProvider = generator.getDefaultBiomeProvider(worldInfo);
        }

        if (console.options.has("forceUpgrade")) {
            net.minecraft.server.Main.convertWorldButItWorks(
                    actualDimension, worldSession, DataFixers.getDataFixer(), worlddimension.generator().getTypeNameForDataFixer(), console.options.has("eraseCache")
            );
        }

        ResourceKey<net.minecraft.world.level.Level> worldKey;
        worldKey = ResourceKey.create(Registries.DIMENSION, new net.minecraft.resources.ResourceLocation(creator.key().getNamespace().toLowerCase(java.util.Locale.ENGLISH), creator.key().getKey().toLowerCase(java.util.Locale.ENGLISH))); // Paper

        ServerLevel internal = new ServerLevel(console, console.executor, worldSession, worlddata, worldKey, worlddimension, console.progressListenerFactory.create(11),
                worlddata.isDebugWorld(), j, creator.environment() == org.bukkit.World.Environment.NORMAL ? list : ImmutableList.of(), true, null, creator.environment(), generator, biomeProvider);

        internal.randomSpawnSelection = new ChunkPos(internal.getChunkSource().randomState().sampler().findSpawnPosition());

        console.addLevel(internal);

        internal.setSpawnSettings(true, true);

        internal.keepSpawnInMemory = creator.keepSpawnLoaded().toBooleanOrElse(internal.getWorld().getKeepSpawnInMemory()); // Paper

        console.prepareLevels(internal.getChunkSource().chunkMap.progressListener, internal);

        io.papermc.paper.threadedregions.RegionizedServer.getInstance().addWorld(internal);

        Bukkit.getPluginManager().callEvent(new WorldLoadEvent(internal.getWorld()));
        return WorldFeedback.Feedback.SUCCESS.toFeedbackWorld(internal.getWorld());
    }

    @Override
    public void resetChunk(World craftWorld, Position position) {
        boolean hasAnyPlayerInChunk = false;
        final ServerLevel serverLevel = ((CraftWorld) craftWorld).getHandle();
        io.papermc.paper.util.TickThread.ensureTickThread(serverLevel, position.x(), position.z(), "Cannot regenerate chunk asynchronously");
        final net.minecraft.server.level.ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
        final ChunkPos chunkPos = new ChunkPos(position.x(), position.z());
        final net.minecraft.world.level.chunk.LevelChunk levelChunk = serverChunkCache.getChunk(chunkPos.x, chunkPos.z, true);
        final Iterable<BlockPos> blockPosIterable = BlockPos.betweenClosed(chunkPos.getMinBlockX(), serverLevel.getMinBuildHeight(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), serverLevel.getMaxBuildHeight() - 1, chunkPos.getMaxBlockZ());
        for (Entity entity : serverLevel.getChunkEntities(position.x(), position.z())) {
            if (entity instanceof Player) {
                hasAnyPlayerInChunk = true;
                continue;
            }
            entity.remove();
        }
        for (final BlockPos blockPos : blockPosIterable) {
            levelChunk.removeBlockEntity(blockPos);
            serverLevel.setBlock(blockPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 16);
            Block block = craftWorld.getBlockAt(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            BlockState state = block.getState();
            if (state instanceof PersistentDataHolder) {
                PersistentDataContainer container = ((PersistentDataHolder) state).getPersistentDataContainer();
                for (NamespacedKey key : container.getKeys()) container.remove(key);
            }
        }
        if (!hasAnyPlayerInChunk) return;
        for (final BlockPos blockPos : blockPosIterable) {
            serverChunkCache.blockChanged(blockPos);
        }
    }

    /**
     * Gets the current location TPS.
     *
     * @param location the location for which to get the TPS
     * @return current location TPS (5s, 15s, 1m, 5m, 15m in Folia-Server), or null if the region doesn't exist
     */
    @Override
    public double @Nullable [] getTPS(Location location) {
        final int x = location.blockX() >> 4;
        final int z = location.blockZ() >> 4;
        final ServerLevel world = ((CraftWorld) location.getWorld()).getHandle();
        return getTPSFromRegion(world, x, z);
    }

    /**
     * Gets the current chunk TPS.
     *
     * @param chunk the chunk for which to get the TPS
     * @return current location TPS (5s, 15s, 1m, 5m, 15m in Folia-Server), or null if the region doesn't exist
     */
    @Override
    public double @Nullable [] getTPS(Chunk chunk) {
        final int x = chunk.getX();
        final int z = chunk.getZ();
        final ServerLevel world = ((CraftWorld) chunk.getWorld()).getHandle();
        return getTPSFromRegion(world, x, z);
    }

    /**
     * Gets the average tick times for a specific location.
     *
     * @param location the location for which to get the average tick times
     * @return an array of average tick times, or null if the region doesn't exist
     */
    @Override
    public double @Nullable [] getAverageTickTimes(Location location) {
        final int x = location.blockX() >> 4;
        final int z = location.blockZ() >> 4;
        final ServerLevel world = ((CraftWorld) location.getWorld()).getHandle();
        return getAverageTickTime(world, x, z);
    }

    /**
     * Gets the average tick times for a specific chunk.
     *
     * @param chunk the chunk for which to get the average tick times
     * @return an array of average tick times, or null if the region doesn't exist
     */
    @Override
    public double @Nullable [] getAverageTickTimes(Chunk chunk) {
        final int x = chunk.getX();
        final int z = chunk.getZ();
        final ServerLevel world = ((CraftWorld) chunk.getWorld()).getHandle();
        return getAverageTickTime(world, x, z);
    }

    private double[] getTPSFromRegion(ServerLevel world, int x, int z) {
        io.papermc.paper.threadedregions.ThreadedRegionizer.ThreadedRegion<io.papermc.paper.threadedregions.TickRegions.TickRegionData, io.papermc.paper.threadedregions.TickRegions.TickRegionSectionData>
                region = world.regioniser.getRegionAtSynchronised(x, z);
        if (region == null) {
            return null;
        } else {
            io.papermc.paper.threadedregions.TickRegions.TickRegionData regionData = region.getData();
            final long currTime = System.nanoTime();
            return new double[]{
                    regionData.getRegionSchedulingHandle().getTickReport5s(currTime).tpsData().segmentAll().average(),
                    regionData.getRegionSchedulingHandle().getTickReport15s(currTime).tpsData().segmentAll().average(),
                    regionData.getRegionSchedulingHandle().getTickReport1m(currTime).tpsData().segmentAll().average(),
                    regionData.getRegionSchedulingHandle().getTickReport5m(currTime).tpsData().segmentAll().average(),
                    regionData.getRegionSchedulingHandle().getTickReport15m(currTime).tpsData().segmentAll().average(),
            };
        }
    }

    private static double[] getAverageTickTime(ServerLevel world, int x, int z) {
        io.papermc.paper.threadedregions.ThreadedRegionizer.ThreadedRegion<io.papermc.paper.threadedregions.TickRegions.TickRegionData, io.papermc.paper.threadedregions.TickRegions.TickRegionSectionData>
                region = world.regioniser.getRegionAtSynchronised(x, z);
        if (region == null) {
            return null;
        } else {
            io.papermc.paper.threadedregions.TickRegions.TickRegionData regionData = region.getData();
            final io.papermc.paper.threadedregions.TickRegionScheduler.RegionScheduleHandle regionScheduleHandle = regionData.getRegionSchedulingHandle();
            final long currTime = System.nanoTime();
            return new double[]{
                    regionScheduleHandle.getTickReport5s(currTime).timePerTickData().segmentAll().average() / 1.0E6,
                    regionScheduleHandle.getTickReport15s(currTime).timePerTickData().segmentAll().average() / 1.0E6,
                    regionScheduleHandle.getTickReport1m(currTime).timePerTickData().segmentAll().average() / 1.0E6,
                    regionScheduleHandle.getTickReport5m(currTime).timePerTickData().segmentAll().average() / 1.0E6,
                    regionScheduleHandle.getTickReport15m(currTime).timePerTickData().segmentAll().average() / 1.0E6,
            };
        }
    }
}
