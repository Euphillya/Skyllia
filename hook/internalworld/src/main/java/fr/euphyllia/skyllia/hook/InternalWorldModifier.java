package fr.euphyllia.skyllia.hook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicSetting;
import fr.euphyllia.skyllia.api.utils.RegionUtils;
import fr.euphyllia.skyllia.api.utils.schematics.SchematicDTO;
import fr.euphyllia.skyllia.api.world.WorldModifier;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public record InternalWorldModifier(JavaPlugin plugin) implements WorldModifier {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final Logger log = LoggerFactory.getLogger(InternalWorldModifier.class);

    private static boolean isAir(BlockData bd) {
        Material m = bd.getMaterial();
        return m == Material.AIR || m == Material.CAVE_AIR || m == Material.VOID_AIR;
    }

    private static void setInt(java.util.function.IntConsumer setter, Object v) {
        if (v instanceof Number n) setter.accept(n.intValue());
    }

    @SuppressWarnings("unchecked")
    private static void applyContainer(Container c, Object invRaw) {
        c.getInventory().clear();
        if (!(invRaw instanceof List<?> list)) return;

        for (Object o : list) {
            if (!(o instanceof Map<?, ?> m)) continue;
            int slot = ((Number) m.get("slot")).intValue();
            Object itemRaw = m.get("item");
            ItemStack item = null;

            if (itemRaw instanceof String s) {
                try {
                    byte[] data = Base64.getDecoder().decode(s);
                    item = ItemStack.deserializeBytes(data);
                } catch (IllegalArgumentException exception) {
                    log.error("Error when loading ItemStack", exception);
                }
            } else if (itemRaw instanceof Map<?, ?> rawMap) {
                Map<String, Object> cast = (Map<String, Object>) rawMap;
                item = ItemStack.deserialize(cast);
            } else {
                log.error("Error when loading ItemStack : {}", itemRaw);
                return;
            }
            if (item != null && !item.getType().isAir()) {
                c.getSnapshotInventory().setItem(slot, item);
                c.update(true);
            }
        }
    }

    /**
     * Paste a schematic at a specific location.
     *
     * @param loc      The location where the schematic will be pasted.
     * @param settings The settings for the schematic paste operation.
     */
    @Override
    public void pasteSchematicWE(@NotNull Location loc, @NotNull SchematicSetting settings) {
        World world = loc.getWorld();

        if (world == null) {
            log.error("World is null for location: {}", loc);
            return;
        }

        File file = new File(plugin.getDataFolder() + File.separator + settings.schematicFile());

        SchematicDTO schematicDTO;
        try (var in = Files.newInputStream(file.toPath());
             var r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            schematicDTO = GSON.fromJson(r, SchematicDTO.class);
        } catch (Exception e) {
            log.error("Failed to read schematic file: {}", file.getAbsolutePath(), e);
            return;
        }

        final Map<Long, List<BlockEntityPlace>> besByChunk = new HashMap<>();
        if (schematicDTO.blockEntities != null) {
            for (var be : schematicDTO.blockEntities) {
                int wx = loc.getBlockX() + (be.x - schematicDTO.origin.x());
                int wy = loc.getBlockY() + (be.y - schematicDTO.origin.y());
                int wz = loc.getBlockZ() + (be.z - schematicDTO.origin.z());
                int cx = wx >> 4, cz = wz >> 4;
                long key = (((long) cx) << 32) ^ (cz & 0xffffffffL);
                besByChunk.computeIfAbsent(key, k -> new ArrayList<>())
                        .add(new BlockEntityPlace(wx, wy, wz, be.kind, be.data));
            }
        }

        final Map<Long, List<EntityPlace>> entsByChunk = new HashMap<>();
        if (schematicDTO.entities != null && settings.copyEntities()) {
            for (var e : schematicDTO.entities) {
                int wx = loc.getBlockX() + ((int) Math.floor(e.x) - schematicDTO.origin.x());
                int wy = loc.getBlockY() + ((int) Math.floor(e.y) - schematicDTO.origin.y());
                int wz = loc.getBlockZ() + ((int) Math.floor(e.z) - schematicDTO.origin.z());
                int cx = wx >> 4, cz = wz >> 4;
                long key = (((long) cx) << 32) ^ (cz & 0xffffffffL);
                entsByChunk.computeIfAbsent(key, k -> new ArrayList<>())
                        .add(new EntityPlace(e.type, wx + (e.x - Math.floor(e.x)),
                                wy + (e.y - Math.floor(e.y)),
                                wz + (e.z - Math.floor(e.z)),
                                e.yaw, e.pitch, e.data));
            }
        }

        Bukkit.getRegionScheduler().execute(plugin, loc, () -> {
            List<BlockData> palette = new ArrayList<>(schematicDTO.palette.size());
            for (String state : schematicDTO.palette) palette.add(Bukkit.createBlockData(state));

            int dx = schematicDTO.size.dx(), dy = schematicDTO.size.dy(), dz = schematicDTO.size.dz();

            int ox = loc.getBlockX() - schematicDTO.origin.x();
            int oy = loc.getBlockY() - schematicDTO.origin.y();
            int oz = loc.getBlockZ() - schematicDTO.origin.z();

            final Map<Long, List<Voxel>> blocksByChunk = new HashMap<>();
            int y = 0, z = 0, x = 0, r = 0, runRemaining = 0, idx = 0;

            while (y < dy) {
                if (runRemaining == 0) {
                    int[] entry = schematicDTO.blocks.get(r++);
                    idx = entry[0];
                    runRemaining = entry[1];
                }
                runRemaining--;

                int wx = ox + x, wy = oy + y, wz = oz + z;

                BlockData bd = palette.get(idx);
                if (!(settings.ignoreAirBlocks() && isAir(bd))) {
                    int cx = wx >> 4, cz = wz >> 4;
                    long key = (((long) cx) << 32) ^ (cz & 0xffffffffL);
                    blocksByChunk.computeIfAbsent(key, k -> new ArrayList<>()).add(new Voxel(wx, wy, wz, bd));
                }

                if (++x >= dx) {
                    x = 0;
                    if (++z >= dz) {
                        z = 0;
                        ++y;
                    }
                }
            }

            for (var e : blocksByChunk.entrySet()) {
                final int cx = (int) (e.getKey() >> 32);
                final int cz = (int) (e.getKey().longValue());
                final List<Voxel> voxels = e.getValue();
                final List<BlockEntityPlace> bes = besByChunk.getOrDefault(e.getKey(), List.of());
                try {
                    for (Voxel v : voxels) {
                        Block b = world.getBlockAt(v.x, v.y, v.z);
                        b.setBlockData(v.bd, false);
                    }
                    for (BlockEntityPlace be : bes) {
                        BlockState bs = world.getBlockAt(be.x, be.y, be.z).getState(true);
                        if (!(bs instanceof TileState ts)) continue;

                        if (ts instanceof Container c) {
                            applyContainer(c, be.data.get("inv"));
                        }

                        if (ts instanceof Sign sign) {
                            Object linesObj = be.data.get("lines");
                            if (linesObj instanceof List<?> lines) {
                                for (int i = 0; i < Math.min(4, lines.size()); i++) {
                                    sign.setLine(i, String.valueOf(lines.get(i)));
                                }
                            }
                            Object color = be.data.get("color");
                            if (color instanceof String col) {
                                try {
                                    sign.setColor(DyeColor.valueOf(col));
                                } catch (Exception ignored) {
                                }
                            }
                            Object glow = be.data.get("glow");
                            if (glow instanceof Boolean g) sign.setGlowingText(g);
                        }
                        if (ts instanceof CreatureSpawner sp) {
                            Object type = be.data.get("type");
                            if (type instanceof String name) {
                                try {
                                    sp.setSpawnedType(EntityType.valueOf(name));
                                } catch (Exception ignored) {
                                }
                            }
                            setInt(sp::setDelay, be.data.get("delay"));
                            setInt(sp::setMinSpawnDelay, be.data.get("minDelay"));
                            setInt(sp::setMaxSpawnDelay, be.data.get("maxDelay"));
                            setInt(sp::setMaxNearbyEntities, be.data.get("maxNearbyEntities"));
                            setInt(sp::setSpawnCount, be.data.get("spawnCount"));
                            setInt(sp::setSpawnRange, be.data.get("spawnRange"));
                            setInt(sp::setRequiredPlayerRange, be.data.get("requiredPlayerRange"));
                        }
                        ts.update(true, false);
                    }
                } catch (Exception ex) {
                    log.error("Failed to paste chunk {}/{}", cx, cz, ex);
                }
            }
            for (List<EntityPlace> b : entsByChunk.values()) {
                for (EntityPlace ep : b) {
                    spawnEntityAPI(world, ep);
                }
            }
        });

    }

    /**
     * Delete an island by replacing its blocks with air.
     *
     * @param island         The island to be deleted.
     * @param world          The world where the island is located.
     * @param regionDistance The distance around the island to be cleared.
     * @param onFinish       A callback function that will be called with a boolean indicating success or failure when the operation is complete.
     */
    @Override
    public void deleteIsland(@NotNull Island island, @NotNull World world, int regionDistance, Consumer<Boolean> onFinish) {
        Position position = island.getPosition();
        List<Position> chunks = RegionUtils.computeChunksToDelete(position, regionDistance, island.getSize());

        if (chunks.isEmpty()) {
            if (onFinish != null) onFinish.accept(true);
            return;
        }

        AtomicInteger toDelete = new AtomicInteger(chunks.size());
        AtomicBoolean failed = new AtomicBoolean(false);
        AtomicInteger delay = new AtomicInteger(1);


        for (Position chunkPos : chunks) {
            Bukkit.getRegionScheduler().runDelayed(plugin, world, chunkPos.x(), chunkPos.z(), task -> {
                try {
                    SkylliaAPI.getWorldNMS().resetChunk(world, chunkPos);
                } catch (Exception e) {
                    failed.set(true);
                }
                if (toDelete.decrementAndGet() == 0 && onFinish != null) {
                    onFinish.accept(!failed.get());
                }
            }, delay.getAndIncrement());
        }
    }

    /**
     * Change the biome of a specific chunk.
     *
     * @param location The location within the chunk to change the biome.
     * @param biome    The new biome to set for the chunk.
     * @return A CompletableFuture that will contain true if the operation was successful, false otherwise.
     */
    @Override
    public CompletableFuture<Boolean> changeBiomeChunk(@NotNull Location location, @NotNull Biome biome) {
        return changeBiomeChunk(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, biome);
    }

    /**
     * Change the biome of a specific chunk.
     *
     * @param world  The world where the chunk is located.
     * @param chunkX The X coordinate of the chunk.
     * @param chunkZ The Z coordinate of the chunk.
     * @param biome  The new biome to set for the chunk.
     * @return A CompletableFuture that will contain true if the operation was successful, false otherwise.
     */
    @Override
    public CompletableFuture<Boolean> changeBiomeChunk(@NotNull World world, int chunkX, int chunkZ, @NotNull Biome biome) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Bukkit.getRegionScheduler().run(SkylliaAPI.getPlugin(), world, chunkX, chunkZ, (task) -> {
            try {
                SkylliaAPI.getBiomesImpl().setBiome(world, chunkX, chunkZ, biome);
            } catch (Exception exception) {
                log.error("Failed to change biome for chunk {}/{} in world {}", chunkX, chunkZ, world.getName(), exception);
                future.complete(false);
            }
        });
        return future;
    }

    /**
     * Change the biome of an entire island.
     *
     * @param world          The world where the island is located.
     * @param biome          The new biome to set for the island.
     * @param island         The island whose biome will be changed.
     * @param regionDistance The distance around the island to be affected.
     * @return A CompletableFuture that will contain true if the operation was successful, false otherwise.
     */
    @Override
    public CompletableFuture<Boolean> changeBiomeIsland(@NotNull World world, @NotNull Biome biome, @NotNull Island island, int regionDistance) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Position islandPos = island.getPosition();

        List<Position> chunks = new ArrayList<>();
        RegionUtils.spiralStartCenter(islandPos, regionDistance, island.getSize(), chunks::add);

        if (chunks.isEmpty()) {
            future.complete(true);
            return future;
        }

        AtomicInteger remaining = new AtomicInteger(chunks.size());
        AtomicBoolean failed = new AtomicBoolean(false);
        AtomicInteger delay = new AtomicInteger(1);

        for (Position chunkPos : chunks) {
            final int cX = chunkPos.x();
            final int cZ = chunkPos.z();
            Bukkit.getRegionScheduler().runDelayed(plugin, world, cX, cZ, task -> {
                try {
                    SkylliaAPI.getBiomesImpl().setBiome(world, cX, cZ, biome);
                } catch (Exception exception) {
                    failed.set(true);
                    log.error("Failed to change biome for chunk {}/{} in world {}", cX, cZ, world.getName(), exception);
                } finally {
                    if (remaining.decrementAndGet() == 0) {
                        future.complete(!failed.get());
                    }
                }
            }, delay.getAndIncrement());
        }
        return future;
    }

    private void spawnEntityAPI(World world, EntityPlace ep) {
        Location loc = new Location(world, ep.x, ep.y, ep.z, ep.yaw, ep.pitch);
        Object snapStr = ep.data.get("snapshot");
        if (snapStr instanceof String s && !s.isEmpty()) {
            if (trySpawnSnapshotFromString(loc, s)) {
                return;
            }
        }
        org.bukkit.entity.EntityType type = null;
        try {
            EntityType entityType = EntityType.valueOf(ep.type);
            world.spawnEntity(loc, entityType);
        } catch (Exception e) {
            log.error("Failed to spawn entity of type {}", ep.type, e);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean trySpawnSnapshotFromString(Location location, String snapshotString) {
        try {
            Class<?> bukkitClz = Class.forName("org.bukkit.Bukkit");
            java.lang.reflect.Method getEntityFactory = bukkitClz.getMethod("getEntityFactory");
            Object factory = getEntityFactory.invoke(null);
            if (factory == null) return false;

            java.lang.reflect.Method createSnap = factory.getClass()
                    .getMethod("createEntitySnapshot", String.class);

            Object snapshot = createSnap.invoke(factory, snapshotString);
            if (snapshot == null) return false;

            java.lang.reflect.Method createEntity = snapshot.getClass()
                    .getMethod("createEntity", Location.class);
            Object created = createEntity.invoke(snapshot, location);

            return created != null;
        } catch (NoSuchMethodException e) {
            log.error("Failed to spawn snapshot of type {}", snapshotString, e);
            return false;
        } catch (Throwable e) {
            log.error("Failed to spawn entity from snapshot", e);
            return false;
        }
    }

    private record Voxel(int x, int y, int z, BlockData bd) {
    }

    private record BlockEntityPlace(int x, int y, int z, String kind, Map<String, Object> data) {
    }

    private record EntityPlace(String type, double x, double y, double z, float yaw, float pitch,
                               Map<String, Object> data) {
    }
}
