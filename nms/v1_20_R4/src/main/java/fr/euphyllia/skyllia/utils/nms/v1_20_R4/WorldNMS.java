package fr.euphyllia.skyllia.utils.nms.v1_20_R4;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.coordinate.ChunkCoordinate;
import fr.euphyllia.skyllia.api.world.WorldFeedback;
import io.papermc.paper.util.TickThread;
import net.kyori.adventure.util.TriState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.phys.AABB;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.generator.CraftWorldInfo;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

public class WorldNMS extends fr.euphyllia.skyllia.api.utils.nms.WorldNMS {

    private static final Logger log = LoggerFactory.getLogger(WorldNMS.class);

    static GameType getGameType(GameMode gameMode) {
        return switch (gameMode) {
            case SURVIVAL -> GameType.SURVIVAL;
            case CREATIVE -> GameType.CREATIVE;
            case ADVENTURE -> GameType.ADVENTURE;
            case SPECTATOR -> GameType.SPECTATOR;
        };
    }

    public static double[] getTPSFromRegion(ServerLevel world, int x, int z) {
        io.papermc.paper.threadedregions.ThreadedRegionizer.ThreadedRegion<io.papermc.paper.threadedregions.TickRegions.TickRegionData, io.papermc.paper.threadedregions.TickRegions.TickRegionSectionData>
                region = world.regioniser.getRegionAtUnsynchronised(x, z);
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
                region = world.regioniser.getRegionAtUnsynchronised(x, z);
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

    @Override
    public WorldFeedback.FeedbackWorld createWorld(WorldCreator creator) {
        return createWorldInternal(creator, null, null);
    }

    @Override
    public WorldFeedback.FeedbackWorld createWorld(WorldCreator creator, WorldConfig worldConfig) {
        if (!worldConfig.hasCustomHeight()) {
            return createWorldInternal(creator, null, null);
        }
        Holder<DimensionType> holder = WorldHeightUtil.registerCustomDimension(creator.name(), worldConfig);
        return createWorldInternal(creator, worldConfig, holder);
    }

    private WorldFeedback.FeedbackWorld createWorldInternal(WorldCreator creator, WorldConfig worldConfig, Holder<DimensionType> customHeightHolder) {
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        DedicatedServer console = craftServer.getServer();
        Preconditions.checkState(console.getAllLevels().iterator().hasNext(), "Cannot create additional worlds on STARTUP");
        Preconditions.checkArgument(creator != null, "WorldCreator cannot be null");

        String name = creator.name();
        ChunkGenerator generator = creator.generator();
        BiomeProvider biomeProvider = creator.biomeProvider();
        File folder = new File(craftServer.getWorldContainer(), name);
        World world = craftServer.getWorld(name);

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
                    MinecraftServer.LOGGER.error("Failed to load world data from {} and {}. World files may be corrupted. Shutting down.",
                            convertable_b.dataFile(), convertable_b.oldDataFile());
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
            LevelDataAndDimensions leveldataanddimensions = LevelStorageSource.getLevelDataAndDimensions(
                    dynamic, worldloader_a.dataConfiguration(), iregistry, worldloader_a.datapackWorldgen());
            worlddata = (PrimaryLevelData) leveldataanddimensions.worldData();
            iregistry = leveldataanddimensions.dimensions().dimensions();
            iregistrycustom_dimension = leveldataanddimensions.dimensions().dimensionsRegistryAccess();
        } else {
            WorldOptions worldoptions = new WorldOptions(creator.seed(), creator.generateStructures(), false);
            DedicatedServerProperties.WorldDimensionData properties = new DedicatedServerProperties.WorldDimensionData(
                    GsonHelper.parse((creator.generatorSettings().isEmpty()) ? "{}" : creator.generatorSettings()),
                    creator.type().name().toLowerCase(Locale.ROOT));
            LevelSettings worldsettings = new LevelSettings(name,
                    getGameType(GameMode.SURVIVAL), hardcore, Difficulty.EASY, false,
                    new GameRules(), worldloader_a.dataConfiguration());
            WorldDimensions worlddimensions = properties.create(worldloader_a.datapackWorldgen());
            WorldDimensions.Complete worlddimensions_b = worlddimensions.bake(iregistry);
            Lifecycle lifecycle = worlddimensions_b.lifecycle().add(worldloader_a.datapackWorldgen().allRegistriesLifecycle());
            worlddata = new PrimaryLevelData(worldsettings, worldoptions, worlddimensions_b.specialWorldProperty(), lifecycle);
            iregistry = worlddimensions_b.dimensions();
            iregistrycustom_dimension = worlddimensions_b.dimensionsRegistryAccess();
        }

        worlddata.customDimensions = iregistry;
        worlddata.checkName(name);
        worlddata.setModdedInfo(console.getServerModName(), console.getModdedStatus().shouldReportAsModified());

        long j = BiomeManager.obfuscateSeed(worlddata.worldGenOptions().seed());
        List<CustomSpawner> list = ImmutableList.of(
                new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(worlddata));

        LevelStem worlddimensionBase = iregistry.get(actualDimension);
        LevelStem worlddimension = (customHeightHolder != null)
                ? new LevelStem(customHeightHolder, worlddimensionBase.generator())
                : worlddimensionBase;

        WorldInfo worldInfo = new CraftWorldInfo(worlddata, worldSession, creator.environment(),
                worlddimension.type().value(), worlddimension.generator(), craftServer.getHandle().getServer().registryAccess());
        if (biomeProvider == null && generator != null) {
            biomeProvider = generator.getDefaultBiomeProvider(worldInfo);
        }

        if (console.options.has("forceUpgrade")) {
            net.minecraft.server.Main.forceUpgrade(worldSession, DataFixers.getDataFixer(),
                    console.options.has("eraseCache"), () -> true, iregistrycustom_dimension,
                    console.options.has("recreateRegionFiles"));
        }

        ResourceKey<net.minecraft.world.level.Level> worldKey;
        String levelName = craftServer.getServer().getProperties().levelName;
        if (name.equals(levelName + "_nether")) {
            worldKey = net.minecraft.world.level.Level.NETHER;
        } else if (name.equals(levelName + "_the_end")) {
            worldKey = net.minecraft.world.level.Level.END;
        } else {
            worldKey = ResourceKey.create(Registries.DIMENSION,
                    new net.minecraft.resources.ResourceLocation(
                            creator.key().getNamespace().toLowerCase(java.util.Locale.ENGLISH),
                            creator.key().getKey().toLowerCase(java.util.Locale.ENGLISH)));
        }

        ServerLevel internal = new ServerLevel(console, console.executor, worldSession, worlddata, worldKey, worlddimension,
                craftServer.getServer().progressListenerFactory.create(11),
                worlddata.isDebugWorld(), j,
                creator.environment() == World.Environment.NORMAL ? list : ImmutableList.of(),
                true, console.overworld().getRandomSequences(), creator.environment(), generator, biomeProvider);

        if (SkylliaAPI.isFolia()) {
            internal.randomSpawnSelection = new ChunkPos(internal.getChunkSource().randomState().sampler().findSpawnPosition());
        }

        console.addLevel(internal);
        internal.setSpawnSettings(true, true);

        if (creator.keepSpawnLoaded() == TriState.FALSE) {
            worlddata.getGameRules().getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(0, null);
        }

        console.prepareLevels(internal.getChunkSource().chunkMap.progressListener, internal);

        if (SkylliaAPI.isFolia()) {
            io.papermc.paper.threadedregions.RegionizedServer.getInstance().addWorld(internal);
        }

        Bukkit.getPluginManager().callEvent(new WorldLoadEvent(internal.getWorld()));
        return WorldFeedback.Feedback.SUCCESS.toFeedbackWorld(internal.getWorld());
    }

    @Override
    public Map<Material, Integer> getCountAllBlocksInChunk(World world, int chunkX, int chunkZ) {
        Map<Material, Integer> blockCounts = new java.util.EnumMap<>(Material.class);
        net.minecraft.server.level.ServerLevel nms = ((CraftWorld) world).getHandle();

        LevelChunk chunk = nms.getChunkSource().getChunkNow(chunkX, chunkZ);
        if (chunk == null) {
            chunk = nms.getChunkSource().getChunk(chunkX, chunkZ, true);
        }
        if (chunk == null) {
            return blockCounts;
        }

        final LevelChunkSection[] sections = chunk.getSections();
        if (sections == null || sections.length == 0) {
            return blockCounts;
        }

        for (LevelChunkSection section : sections) {
            if (section == null || section.hasOnlyAir()) continue;

            section.states.count((state, count) -> {
                if (state.isAir()) return;
                Material mat = state.getBukkitMaterial();
                blockCounts.merge(mat, count, Integer::sum);
            });
        }

        return blockCounts;
    }

    @Override
    public void resetChunk(World craftWorld, ChunkCoordinate position) {
        final ServerLevel nms = ((CraftWorld) craftWorld).getHandle();
        final int chunkX = position.x();
        final int chunkZ = position.z();

        TickThread.ensureTickThread(nms, chunkX, chunkZ, "Cannot reset chunk asynchronously");

        LevelChunk chunk = nms.getChunkSource().getChunkNow(chunkX, chunkZ);
        if (chunk == null) {
            chunk = nms.getChunkSource().getChunk(chunkX, chunkZ, true);
        }
        if (chunk == null) {
            log.error("Cannot reset chunk asynchronously");
            return;
        }

        boolean hasAnyPlayer = false;
        for (Entity entity : nms.getChunkEntities(chunkX, chunkZ)) {
            if (entity instanceof Player) {
                hasAnyPlayer = true;
            } else {
                entity.remove();
            }
        }

        final LevelChunkSection[] sections = chunk.getSections();
        if (sections == null || sections.length == 0) {
            return;
        }

        final int baseX = chunkX << 4;
        final int baseZ = chunkZ << 4;
        final net.minecraft.world.level.block.state.BlockState airState = Blocks.AIR.defaultBlockState();

        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            final LevelChunkSection section = sections[sectionIndex];
            if (section == null || section.hasOnlyAir()) {
                continue;
            }

            final int sectionY = chunk.getSectionYFromSectionIndex(sectionIndex);
            final int baseY = sectionY << 4;

            for (int ly = 0; ly < 16; ly++) {
                final int worldY = baseY + ly;
                for (int lz = 0; lz < 16; lz++) {
                    final int worldZ = baseZ + lz;
                    for (int lx = 0; lx < 16; lx++) {
                        final int worldX = baseX + lx;

                        final net.minecraft.world.level.block.state.BlockState state = section.getBlockState(lx, ly, lz);
                        if (state.isAir()) {
                            continue;
                        }
                        final BlockPos blockPos = new BlockPos(worldX, worldY, worldZ);

                        Block block = craftWorld.getBlockAt(worldX, worldY, worldZ);
                        org.bukkit.block.BlockState bukkitState = block.getState(false);
                        if (bukkitState instanceof PersistentDataHolder) {
                            PersistentDataContainer container = ((PersistentDataHolder) bukkitState).getPersistentDataContainer();
                            for (NamespacedKey key : container.getKeys()) {
                                container.remove(key);
                            }
                        }
                        chunk.removeBlockEntity(blockPos);
                        section.setBlockState(lx, ly, lz, airState, false);
                    }
                }
            }

            section.recalcBlockCounts();
        }

        if (hasAnyPlayer) {
            LevelChunk finalChunk = chunk;
            nms.getChunkSource().chunkMap.getPlayers(new ChunkPos(chunkX, chunkZ), false)
                    .forEach(player -> player.connection.send(
                            new ClientboundLevelChunkWithLightPacket(finalChunk, nms.getLightEngine(), null, null)
                    ));
        }

        chunk.setUnsaved(true);
    }

    @Override
    public double @Nullable [] getTPS(Location location) {
        final int x = location.blockX() >> 4;
        final int z = location.blockZ() >> 4;
        final ServerLevel world = ((CraftWorld) location.getWorld()).getHandle();
        return getTPSFromRegion(world, x, z);
    }

    @Override
    public double @Nullable [] getTPS(Chunk chunk) {
        final int x = chunk.getX();
        final int z = chunk.getZ();
        final ServerLevel world = ((CraftWorld) chunk.getWorld()).getHandle();
        return getTPSFromRegion(world, x, z);
    }

    @Override
    public double @Nullable [] getAverageTickTimes(Location location) {
        final int x = location.blockX() >> 4;
        final int z = location.blockZ() >> 4;
        final ServerLevel world = ((CraftWorld) location.getWorld()).getHandle();
        return getAverageTickTime(world, x, z);
    }

    @Override
    public double @Nullable [] getAverageTickTimes(Chunk chunk) {
        final int x = chunk.getX();
        final int z = chunk.getZ();
        final ServerLevel world = ((CraftWorld) chunk.getWorld()).getHandle();
        return getAverageTickTime(world, x, z);
    }

    @Override
    public List<Entity> getEntities(World craftWorld, final @Nullable Entity except, final BoundingBox boundingBox, Predicate<? super Entity> filter) {
        final ServerLevel nms = ((CraftWorld) craftWorld).getHandle();
        AABB bb = new AABB(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
        final List<net.minecraft.world.entity.Entity> entityList = new java.util.ArrayList<>();

        net.minecraft.world.entity.Entity exceptNms = except != null ? ((CraftEntity) except).getHandle() : null;
        nms.getEntityLookup().getEntities(exceptNms, bb, entityList, Predicates.alwaysTrue());

        List<Entity> bukkitEntityList = new ArrayList<>(entityList.size());

        for (net.minecraft.world.entity.Entity entity : entityList) {
            Entity bukkitEntity = entity.getBukkitEntity();
            if (filter == null || filter.test(bukkitEntity)) {
                bukkitEntityList.add(bukkitEntity);
            }
        }

        return bukkitEntityList;
    }
}