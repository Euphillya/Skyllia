package fr.euphyllia.skyllia.hook.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.hooks.SchematicHook;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicSetting;
import fr.euphyllia.skyllia.api.utils.schematics.SchematicDTO;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class InternalSchematicHook implements SchematicHook {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final Logger log = LoggerFactory.getLogger(InternalSchematicHook.class);

    private final Plugin plugin;

    public InternalSchematicHook() {
        this.plugin = SkylliaAPI.getPlugin();
    }

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
                    item = ItemStack.deserializeBytes(Base64.getDecoder().decode(s));
                } catch (IllegalArgumentException ex) {
                    log.error("Error when loading ItemStack", ex);
                }
            } else if (itemRaw instanceof Map<?, ?> rawMap) {
                item = ItemStack.deserialize((Map<String, Object>) rawMap);
            }
            if (item != null && !item.getType().isAir()) {
                c.getSnapshotInventory().setItem(slot, item);
                c.update(true);
            }
        }
    }

    @Override
    public String name() {
        return "Internal";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public CompletableFuture<Boolean> paste(@NotNull Location loc, @NotNull SchematicSetting settings) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        World world = loc.getWorld();

        if (world == null) {
            log.error("World is null for location: {}", loc);
            future.complete(false);
            return future;
        }

        File file = new File(plugin.getDataFolder() + File.separator + settings.schematicFile());
        SchematicDTO schematicDTO;
        try (var in = Files.newInputStream(file.toPath());
             var r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            schematicDTO = GSON.fromJson(r, SchematicDTO.class);
        } catch (Exception e) {
            log.error("Failed to read schematic file: {}", file.getAbsolutePath(), e);
            future.complete(false);
            return future;
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
            try {
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
                        for (Voxel v : voxels) world.getBlockAt(v.x, v.y, v.z).setBlockData(v.bd, false);
                        for (BlockEntityPlace be : bes) {
                            BlockState bs = world.getBlockAt(be.x, be.y, be.z).getState(true);
                            if (!(bs instanceof TileState ts)) continue;
                            if (ts instanceof Container c) applyContainer(c, be.data.get("inv"));
                            if (ts instanceof Sign sign) {
                                if (be.data.get("lines") instanceof List<?> lines)
                                    for (int i = 0; i < Math.min(4, lines.size()); i++)
                                        sign.setLine(i, String.valueOf(lines.get(i)));
                                if (be.data.get("color") instanceof String col)
                                    try {
                                        sign.setColor(DyeColor.valueOf(col));
                                    } catch (Exception ignored) {
                                    }
                                if (be.data.get("glow") instanceof Boolean g) sign.setGlowingText(g);
                            }
                            if (ts instanceof CreatureSpawner sp) {
                                if (be.data.get("type") instanceof String name)
                                    try {
                                        sp.setSpawnedType(EntityType.valueOf(name));
                                    } catch (Exception ignored) {
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

                for (List<EntityPlace> b : entsByChunk.values())
                    for (EntityPlace ep : b) spawnEntity(world, ep);

                future.complete(true);
            } catch (Exception e) {
                log.error("Failed to paste schematic at {}: {}", loc, e.getMessage(), e);
                future.complete(false);
            }
        });

        return future;
    }

    private void spawnEntity(World world, EntityPlace ep) {
        Location loc = new Location(world, ep.x, ep.y, ep.z, ep.yaw, ep.pitch);
        if (ep.data.get("snapshot") instanceof String s && !s.isEmpty() && trySpawnSnapshot(loc, s)) return;
        try {
            world.spawnEntity(loc, EntityType.valueOf(ep.type));
        } catch (Exception e) {
            log.error("Failed to spawn entity of type {}", ep.type, e);
        }
    }

    private boolean trySpawnSnapshot(Location location, String snapshotString) {
        try {
            Object factory = Class.forName("org.bukkit.Bukkit").getMethod("getEntityFactory").invoke(null);
            if (factory == null) return false;
            Object snapshot = factory.getClass().getMethod("createEntitySnapshot", String.class).invoke(factory, snapshotString);
            if (snapshot == null) return false;
            return snapshot.getClass().getMethod("createEntity", Location.class).invoke(snapshot, location) != null;
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
