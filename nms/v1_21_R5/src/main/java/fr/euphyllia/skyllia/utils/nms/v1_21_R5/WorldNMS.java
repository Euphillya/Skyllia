package fr.euphyllia.skyllia.utils.nms.v1_21_R5;

import ca.spottedleaf.moonrise.common.util.TickThread;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.coordinate.ChunkCoordinate;
import fr.euphyllia.skyllia.api.world.WorldFeedback;
import io.papermc.paper.FeatureHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
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
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
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

    public static double[] getAverageTickTime(final ServerLevel world, final int x, final int z) {
        final io.papermc.paper.threadedregions.ThreadedRegionizer.ThreadedRegion<io.papermc.paper.threadedregions.TickRegions.TickRegionData, io.papermc.paper.threadedregions.TickRegions.TickRegionSectionData>
                region = world.regioniser.getRegionAtUnsynchronised(x, z);
        if (region == null) {
            return null;
        } else {
            final io.papermc.paper.threadedregions.TickRegions.TickRegionData regionData = region.getData();
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

    public static double[] TPS(final Location location) {
        return Bukkit.getRegionTPS(location);
    }

    public static double[] TPS(final Chunk chunk) {
        return Bukkit.getRegionTPS(chunk);
    }

    public static void initWorld(final ServerLevel serverLevel, final DedicatedServer console, final PrimaryLevelData primaryLevelData) {
        if (!SkylliaAPI.isFolia()) {
            console.initWorld(serverLevel, primaryLevelData, primaryLevelData, primaryLevelData.worldGenOptions());
            return;
        }
        final var x = serverLevel.randomSpawnSelection.x;
        final var z = serverLevel.randomSpawnSelection.z;
        Bukkit.getRegionScheduler().run(SkylliaAPI.getPlugin(), serverLevel.getWorld(), x, z, task -> {
            console.initWorld(serverLevel, primaryLevelData, primaryLevelData, primaryLevelData.worldGenOptions());
        });
    }

    @Override
    public WorldFeedback.FeedbackWorld createWorld(final WorldCreator creator) {
        return createWorldInternal(creator, null, null);
    }

    @Override
    public WorldFeedback.FeedbackWorld createWorld(final WorldCreator creator, final WorldConfig worldConfig) {
        if (!worldConfig.hasCustomHeight()) {
            return createWorldInternal(creator, null, null);
        }
        final Holder<DimensionType> holder = WorldHeightUtil.registerCustomDimension(creator.name(), worldConfig);
        return createWorldInternal(creator, worldConfig, holder);
    }

    private WorldFeedback.FeedbackWorld createWorldInternal(final WorldCreator creator, final WorldConfig worldConfig, final Holder<DimensionType> customHeightHolder) {
        final CraftServer craftServer = (CraftServer) Bukkit.getServer();
        final DedicatedServer console = craftServer.getServer();
        Preconditions.checkState(console.getAllLevels().iterator().hasNext(), "Cannot create additional worlds on STARTUP");
        Preconditions.checkArgument(creator != null, "WorldCreator cannot be null");

        final String name = creator.name();
        ChunkGenerator chunkGenerator = creator.generator();
        BiomeProvider biomeProvider = creator.biomeProvider();
        final File folder = new File(craftServer.getWorldContainer(), name);
        final World world = craftServer.getWorld(name);

        final World worldByKey = craftServer.getWorld(creator.key());
        if (world != null || worldByKey != null) {
            if (world != worldByKey) {
                return WorldFeedback.Feedback.WORLD_DUPLICATED.toFeedbackWorld();
            }
        }

        if ((folder.exists()) && (!folder.isDirectory())) {
            return WorldFeedback.Feedback.WORLD_FOLDER_INVALID.toFeedbackWorld();
        }

        if (chunkGenerator == null) {
            chunkGenerator = craftServer.getGenerator(name);
        }

        if (biomeProvider == null) {
            biomeProvider = craftServer.getBiomeProvider(name);
        }

        final ResourceKey<LevelStem> actualDimension = switch (creator.environment()) {
            case NORMAL -> LevelStem.OVERWORLD;
            case NETHER -> LevelStem.NETHER;
            case THE_END -> LevelStem.END;
            default -> throw new IllegalArgumentException("Illegal dimension (" + creator.environment() + ")");
        };

        final LevelStorageSource.LevelStorageAccess levelStorageAccess;
        try {
            levelStorageAccess = LevelStorageSource.createDefault(craftServer.getWorldContainer().toPath()).validateAndCreateAccess(name, actualDimension);
        } catch (final IOException | ContentValidationException ex) {
            throw new RuntimeException(ex);
        }

        Dynamic<?> dataTag;
        if (levelStorageAccess.hasWorldData()) {
            net.minecraft.world.level.storage.LevelSummary summary;
            try {
                dataTag = levelStorageAccess.getDataTag();
                summary = levelStorageAccess.getSummary(dataTag);
            } catch (final NbtException | ReportedNbtException | IOException e) {
                final LevelStorageSource.LevelDirectory levelDirectory = levelStorageAccess.getLevelDirectory();
                MinecraftServer.LOGGER.warn("Failed to load world data from {}", levelDirectory.dataFile(), e);
                MinecraftServer.LOGGER.info("Attempting to use fallback");
                try {
                    dataTag = levelStorageAccess.getDataTagFallback();
                    summary = levelStorageAccess.getSummary(dataTag);
                } catch (final NbtException | ReportedNbtException | IOException e1) {
                    MinecraftServer.LOGGER.error("Failed to load world data from {}", levelDirectory.oldDataFile(), e1);
                    MinecraftServer.LOGGER.error("Failed to load world data from {} and {}. World files may be corrupted. Shutting down.",
                            levelDirectory.dataFile(), levelDirectory.oldDataFile());
                    return null;
                }
                levelStorageAccess.restoreLevelDataFromOld();
            }
            if (summary.requiresManualConversion()) {
                MinecraftServer.LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                return null;
            }
            if (!summary.isCompatible()) {
                MinecraftServer.LOGGER.info("This world was created by an incompatible version.");
                return null;
            }
        } else {
            dataTag = null;
        }

        final boolean hardcore = creator.hardcore();

        final PrimaryLevelData primaryLevelData;
        final WorldLoader.DataLoadContext context = console.worldLoader;
        RegistryAccess.Frozen registryAccess = context.datapackDimensions();
        net.minecraft.core.Registry<LevelStem> contextLevelStemRegistry = registryAccess.lookupOrThrow(Registries.LEVEL_STEM);
        if (dataTag != null) {
            final LevelDataAndDimensions levelDataAndDimensions = LevelStorageSource.getLevelDataAndDimensions(
                    dataTag, context.dataConfiguration(), contextLevelStemRegistry, context.datapackWorldgen()
            );
            primaryLevelData = (PrimaryLevelData) levelDataAndDimensions.worldData();
            registryAccess = levelDataAndDimensions.dimensions().dimensionsRegistryAccess();
        } else {
            final WorldOptions worldOptions = new WorldOptions(creator.seed(), creator.generateStructures(), false);
            final DedicatedServerProperties.WorldDimensionData properties = new DedicatedServerProperties.WorldDimensionData(
                    GsonHelper.parse((creator.generatorSettings().isEmpty()) ? "{}" : creator.generatorSettings()),
                    creator.type().name().toLowerCase(Locale.ROOT));
            final LevelSettings levelSettings = new LevelSettings(
                    name,
                    GameType.byId(craftServer.getDefaultGameMode().getValue()),
                    hardcore, Difficulty.EASY,
                    false,
                    new GameRules(context.dataConfiguration().enabledFeatures()),
                    context.dataConfiguration());
            final WorldDimensions worldDimensions = properties.create(context.datapackWorldgen());
            final WorldDimensions.Complete complete = worldDimensions.bake(contextLevelStemRegistry);
            final Lifecycle lifecycle = complete.lifecycle().add(context.datapackWorldgen().allRegistriesLifecycle());
            primaryLevelData = new PrimaryLevelData(levelSettings, worldOptions, complete.specialWorldProperty(), lifecycle);
            registryAccess = complete.dimensionsRegistryAccess();
        }

        contextLevelStemRegistry = registryAccess.lookupOrThrow(Registries.LEVEL_STEM);
        primaryLevelData.customDimensions = contextLevelStemRegistry;
        primaryLevelData.checkName(name);
        primaryLevelData.setModdedInfo(console.getServerModName(), console.getModdedStatus().shouldReportAsModified());

        if (console.options.has("forceUpgrade")) {
            net.minecraft.server.Main.forceUpgrade(levelStorageAccess, primaryLevelData, DataFixers.getDataFixer(),
                    console.options.has("eraseCache"), () -> true, registryAccess, console.options.has("recreateRegionFiles"));
        }

        final long i = BiomeManager.obfuscateSeed(primaryLevelData.worldGenOptions().seed());
        final List<CustomSpawner> list = ImmutableList.of(
                new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(primaryLevelData)
        );

        final LevelStem baseStem = contextLevelStemRegistry.getValue(actualDimension);
        final LevelStem customStem = (customHeightHolder != null)
                ? new LevelStem(customHeightHolder, baseStem.generator())
                : baseStem;

        final WorldInfo worldInfo = new CraftWorldInfo(primaryLevelData, levelStorageAccess, creator.environment(),
                customStem.type().value(), customStem.generator(), craftServer.getHandle().getServer().registryAccess());
        if (biomeProvider == null && chunkGenerator != null) {
            biomeProvider = chunkGenerator.getDefaultBiomeProvider(worldInfo);
        }

        final ResourceKey<net.minecraft.world.level.Level> dimensionKey;
        final String levelName = craftServer.getServer().getProperties().levelName;
        if (name.equals(levelName + "_nether")) {
            dimensionKey = net.minecraft.world.level.Level.NETHER;
        } else if (name.equals(levelName + "_the_end")) {
            dimensionKey = net.minecraft.world.level.Level.END;
        } else {
            dimensionKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(creator.key().namespace(), creator.key().value()));
        }

        if (creator.keepSpawnLoaded() == net.kyori.adventure.util.TriState.FALSE) {
            primaryLevelData.getGameRules().getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(0, null);
        }

        final ServerLevel serverLevel = new ServerLevel(
                console,
                console.executor,
                levelStorageAccess,
                primaryLevelData,
                dimensionKey,
                customStem,
                craftServer.getServer().progressListenerFactory.create(primaryLevelData.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS)),
                primaryLevelData.isDebugWorld(),
                i,
                creator.environment() == World.Environment.NORMAL ? list : ImmutableList.of(),
                true,
                console.overworld().getRandomSequences(),
                creator.environment(),
                chunkGenerator, biomeProvider
        );

        console.addLevel(serverLevel);

        if (SkylliaAPI.isFolia()) {
            serverLevel.randomSpawnSelection = new ChunkPos(serverLevel.getChunkSource().randomState().sampler().findSpawnPosition());
        }

        initWorld(serverLevel, console, primaryLevelData);

        serverLevel.setSpawnSettings(true);

        console.prepareLevels(serverLevel.getChunkSource().chunkMap.progressListener, serverLevel);

        if (SkylliaAPI.isFolia()) {
            io.papermc.paper.threadedregions.RegionizedServer.getInstance().addWorld(serverLevel);
        }

        FeatureHooks.tickEntityManager(serverLevel);
        new WorldLoadEvent(serverLevel.getWorld()).callEvent();
        return WorldFeedback.Feedback.SUCCESS.toFeedbackWorld(serverLevel.getWorld());
    }

    @Override
    public Map<Material, Integer> getCountAllBlocksInChunk(final World world, final int chunkX, final int chunkZ) {
        final Map<Material, Integer> blockCounts = new java.util.EnumMap<>(Material.class);
        final net.minecraft.server.level.ServerLevel nms = ((CraftWorld) world).getHandle();

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

        for (final LevelChunkSection section : sections) {
            if (section == null || section.hasOnlyAir()) continue;

            section.states.count((state, count) -> {
                if (state.isAir()) return;
                final Material mat = state.getBukkitMaterial();
                blockCounts.merge(mat, count, Integer::sum);
            });
        }

        return blockCounts;
    }

    @Override
    public void resetChunk(final World craftWorld, final ChunkCoordinate position) {
        final ServerLevel nms = ((CraftWorld) craftWorld).getHandle();
        final int chunkX = position.x();
        final int chunkZ = position.z();

        LevelChunk chunk = nms.getChunkSource().getChunkNow(chunkX, chunkZ);
        if (chunk == null) {
            chunk = nms.getChunkSource().getChunk(chunkX, chunkZ, true);
        }
        if (chunk == null) {
            TickThread.ensureTickThread(nms, chunkX, chunkZ, "Cannot reset chunk asynchronously");
            return;
        }

        boolean hasAnyPlayer = false;
        for (final Entity entity : nms.getChunkEntities(chunkX, chunkZ)) {
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

                        final Block block = craftWorld.getBlockAt(worldX, worldY, worldZ);
                        final org.bukkit.block.BlockState bukkitState = block.getState(false);
                        if (bukkitState instanceof PersistentDataHolder) {
                            final PersistentDataContainer container = ((PersistentDataHolder) bukkitState).getPersistentDataContainer();
                            for (final NamespacedKey key : container.getKeys()) {
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
            final LevelChunk finalChunk = chunk;
            nms.getChunkSource().chunkMap.getPlayers(new ChunkPos(chunkX, chunkZ), false)
                    .forEach(player -> player.connection.send(
                            new ClientboundLevelChunkWithLightPacket(finalChunk, nms.getLightEngine(), null, null)
                    ));
        }

        chunk.markUnsaved();
    }

    @Override
    public double @Nullable [] getTPS(final Location location) {
        return Bukkit.getRegionTPS(location);
    }

    @Override
    public double @Nullable [] getTPS(final Chunk chunk) {
        return Bukkit.getRegionTPS(chunk);
    }

    @Override
    public double @Nullable [] getAverageTickTimes(final Location location) {
        final int x = location.blockX() >> 4;
        final int z = location.blockZ() >> 4;
        final ServerLevel world = ((CraftWorld) location.getWorld()).getHandle();
        return getAverageTickTime(world, x, z);
    }

    @Override
    public double @Nullable [] getAverageTickTimes(final Chunk chunk) {
        final int x = chunk.getX();
        final int z = chunk.getZ();
        final ServerLevel world = ((CraftWorld) chunk.getWorld()).getHandle();
        return getAverageTickTime(world, x, z);
    }

    @Override
    public List<Entity> getEntities(final World craftWorld, final @Nullable Entity except, final BoundingBox boundingBox, final Predicate<? super Entity> filter) {
        final ServerLevel nms = ((CraftWorld) craftWorld).getHandle();
        final AABB bb = new AABB(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
        final List<net.minecraft.world.entity.Entity> entityList = new java.util.ArrayList<>();

        final net.minecraft.world.entity.Entity exceptNms = except != null ? ((CraftEntity) except).getHandle() : null;
        nms.moonrise$getEntityLookup().getEntities(exceptNms, bb, entityList, Predicates.alwaysTrue());

        final List<Entity> bukkitEntityList = new ArrayList<>(entityList.size());

        for (final net.minecraft.world.entity.Entity entity : entityList) {
            final Entity bukkitEntity = entity.getBukkitEntity();
            if (filter == null || filter.test(bukkitEntity)) {
                bukkitEntityList.add(bukkitEntity);
            }
        }

        return bukkitEntityList;
    }
}