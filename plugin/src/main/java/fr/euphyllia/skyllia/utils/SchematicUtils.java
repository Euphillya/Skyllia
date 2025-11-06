package fr.euphyllia.skyllia.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.utils.schematics.*;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SchematicUtils {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final Logger log = LoggerFactory.getLogger(SchematicUtils.class);

    private SchematicUtils() {
    }

    public static void createSchematic(CommandSender sender, Location pos1, Location pos2, Location spawnLocation, String schematicName) {
        // Pos1 et Pos2 doivent être dans le même monde
        World world = pos1.getWorld();
        if (!world.equals(pos2.getWorld())) {
            throw new IllegalArgumentException("Positions must be in the same world.");
        }

        if (!(Bukkit.isOwnedByCurrentRegion(pos1) && Bukkit.isOwnedByCurrentRegion(pos2))) {
            // Mauvais thread
            throw new IllegalStateException("This method must run on the owning region thread.");
        }

        // On va sauvegarder toutes les positions possibles entre pos1 et pos2
        int x1 = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int y1 = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int z1 = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int x2 = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int y2 = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int z2 = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        int dx = x2 - x1 + 1;
        int dy = y2 - y1 + 1;
        int dz = z2 - z1 + 1;

        int sx = spawnLocation.getBlockX();
        int sy = spawnLocation.getBlockY();
        int sz = spawnLocation.getBlockZ();

        int ox = Math.max(0, Math.min(dx - 1, sx - x1));
        int oy = Math.max(0, Math.min(dy - 1, sy - y1));
        int oz = Math.max(0, Math.min(dz - 1, sz - z1));

        if (sx < x1 || sx > x2 || sy < y1 || sy > y2 || sz < z1 || sz > z2) {
            throw new IllegalArgumentException("spawnLocation must be inside the selection box.");
        }

        SchematicDTO out = new SchematicDTO();
        out.origin = new Vec3i(ox, oy, oz);
        out.size = new Size3i(dx, dy, dz);
        out.palette = new ArrayList<>();
        out.blocks = new ArrayList<>();
        out.blockEntities = new ArrayList<>();

        Map<String, Integer> paletteIndex = new LinkedHashMap<>();

        int currentIndex = -1;
        int run = 0;

        for (int y = 0; y < dy; y++) {
            for (int z = 0; z < dz; z++) {
                for (int x = 0; x < dx; x++) {
                    Block b = world.getBlockAt(x1 + x, y1 + y, z1 + z);
                    String state = b.getBlockData().getAsString();

                    int idx = paletteIndex.computeIfAbsent(state, s -> {
                        out.palette.add(s);
                        return out.palette.size() - 1;
                    });

                    if (idx == currentIndex) {
                        run++;
                    } else {
                        if (currentIndex != -1) out.blocks.add(new int[]{currentIndex, run});
                        currentIndex = idx;
                        run = 1;
                    }

                    BlockState blockState = b.getState(false);
                    if (blockState instanceof TileState tileState) {
                        BlockEntityDTO dto = new BlockEntityDTO();
                        dto.x = x;
                        dto.y = y;
                        dto.z = z;
                        dto.kind = b.getType().getKey().toString();
                        dto.data = serializeTileStateAPI(tileState);
                        out.blockEntities.add(dto);
                    }
                }
            }
        }

        var minX = Math.min(pos1.getX(), pos2.getX());
        var minY = Math.min(pos1.getY(), pos2.getY());
        var minZ = Math.min(pos1.getZ(), pos2.getZ());
        var maxX = Math.max(pos1.getX(), pos2.getX()) + 1;
        var maxY = Math.max(pos1.getY(), pos2.getY()) + 1;
        var maxZ = Math.max(pos1.getZ(), pos2.getZ()) + 1;

        out.entities = new ArrayList<>();
        var box = org.bukkit.util.BoundingBox.of(
                new org.bukkit.util.Vector(minX, minY, minZ),
                new org.bukkit.util.Vector(maxX, maxY, maxZ)
        );

        for (Entity entity1 : world.getNearbyEntities(box, entity -> !(entity instanceof Player))) {
            var dto = new EntityDTO();
            dto.type = entity1.getType().name();
            dto.x = entity1.getLocation().getX() - x1;
            dto.y = entity1.getLocation().getY() - y1;
            dto.z = entity1.getLocation().getZ() - z1;
            dto.yaw = entity1.getLocation().getYaw();
            dto.pitch = entity1.getLocation().getPitch();
            dto.data = serializeEntityAPI(entity1);
            out.entities.add(dto);
        }

        if (run > 0) out.blocks.add(new int[]{currentIndex, run});

        Bukkit.getAsyncScheduler().runNow(Skyllia.getInstance(), task -> saveSchematic(sender, schematicName, out));
    }

    private static void saveSchematic(CommandSender sender, String schematicName, SchematicDTO out) {
        Path dir = Skyllia.getInstance().getDataFolder().toPath().resolve("schematics");
        Path file = dir.resolve(schematicName + ".json");
        try {
            Files.createDirectories(dir);
            try (var writer = new OutputStreamWriter(new FileOutputStream(file.toFile()))) {
                GSON.toJson(out, writer);
            }
            ConfigLoader.language.sendMessage(sender, "island.admin.schematic.save-success", Map.of(
                    "%schematic_name%", schematicName
            ));
        } catch (Exception exception) {
            log.error("Failed to save schematic {}", schematicName, exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> serializeTileStateAPI(TileState ts) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (ts instanceof Container c) {
            List<Map<String, Object>> inv = new ArrayList<>();
            var snap = c.getSnapshotInventory();
            for (int i = 0; i < snap.getSize(); i++) {
                ItemStack it = snap.getItem(i);
                if (it == null || it.getType().isAir()) continue;
                Map<String, Object> e = new LinkedHashMap<>();
                String serializedItem = Base64.getEncoder().encodeToString(it.serializeAsBytes());
                e.put("slot", i);
                e.put("item", serializedItem);
                inv.add(e);
            }
            m.put("inv", inv);
        }
        if (ts instanceof Sign sign) {
            m.put("lines", Arrays.asList(sign.getLines()));
            m.put("color", sign.getColor().name());
            m.put("glow", sign.isGlowingText());
        }
        if (ts instanceof CreatureSpawner spawner) {
            if (spawner.getSpawnedType() != null) {
                m.put("type", spawner.getSpawnedType().name());
            }
            m.put("delay", spawner.getDelay());
            m.put("minDelay", spawner.getMinSpawnDelay());
            m.put("maxDelay", spawner.getMaxSpawnDelay());
            m.put("maxNearbyEntities", spawner.getMaxNearbyEntities());
            m.put("spawnCount", spawner.getSpawnCount());
            m.put("spawnRange", spawner.getSpawnRange());
            m.put("requiredPlayerRange", spawner.getRequiredPlayerRange());
        }
        return m;
    }

    private static Map<String, Object> serializeEntityAPI(Entity entity) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", entity.getType().getKey().toString());
        m.put("yaw", entity.getLocation().getYaw());
        m.put("pitch", entity.getLocation().getPitch());

        try {
            var snapshot = entity.createSnapshot();
            if (snapshot != null) {
                m.put("snapshot", snapshot.getAsString());
            }
        } catch (Exception e) {
            log.warn("Failed to serialize entity snapshot for entity of type {}", entity.getType(), e);
        }
        return m;
    }
}
