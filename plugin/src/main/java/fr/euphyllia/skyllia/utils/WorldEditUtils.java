package fr.euphyllia.skyllia.utils;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.World;
import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicWorld;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class WorldEditUtils {

    private static final LinkedHashMap<File, ClipboardFormat> cachedIslandSchematic = new LinkedHashMap<>();
    private static final Logger logger = LogManager.getLogger(WorldEditUtils.class);

    public static Type worldEditVersion() {
        if (Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit")) {
            return Type.FAST_ASYNC_WORLD_EDIT;
        } else if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            return Type.WORLD_EDIT;
        }
        return Type.UNDEFINED;
    }

    public static void pasteSchematicWE(InterneAPI api, Location loc, SchematicWorld schematicWorld) {
        try {
            File file = new File(api.getPlugin().getDataFolder() + File.separator + schematicWorld.schematicFile());
            ClipboardFormat format = cachedIslandSchematic.getOrDefault(file, ClipboardFormats.findByFile(file));
            try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                Clipboard clipboard = reader.read();
                World w = BukkitAdapter.adapt(loc.getWorld());
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(w)) {
                    editSession.setSideEffectApplier(SideEffectSet.defaults());
                    editSession.setReorderMode(EditSession.ReorderMode.MULTI_STAGE);
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()))
                            .copyEntities(true) // Si la schem a des entités
                            .ignoreAirBlocks(true) // On ne colle pas les blocks d'air de la schematic, gain de performance accru
                            .build();
                    cachedIslandSchematic.putIfAbsent(file, format);
                    Operations.complete(operation);
                }
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
        }
    }

    public static void deleteIsland(Main plugin, Island island, org.bukkit.World w) {
        if (w == null) {
            throw new RuntimeException("World is not loaded or not exist");
        }
        Position position = island.getPosition();

        AtomicInteger delay = new AtomicInteger(1);
        int regionSize = (32* ConfigToml.regionDistance) + 1; // une region à une taille de 32, mais un chunk n'est jamais au centre ! Donc je rajoute une marge d'erreur qui sera vérifier dans la spirale
        RegionUtils.spiralStartCenter(position, regionSize, chunKPosition -> {
            Bukkit.getRegionScheduler().runDelayed(plugin, w, chunKPosition.x(), chunKPosition.z(), task ->
                    plugin.getInterneAPI().getWorldNMS().resetChunk(w, chunKPosition), delay.getAndIncrement());
        });
    }

    public static CompletableFuture<Boolean> changeBiomeChunk(Main plugin, org.bukkit.World world, Biome biome, Position position) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        int blockX = position.x() << 4;
        int blockZ = position.z() << 4;
        BlockVector3 pos1 = BlockVector3.at(blockX, world.getMinHeight(), blockZ);
        BlockVector3 pos2 = BlockVector3.at(blockX + 15, world.getMaxHeight(), blockZ + 15);
        CuboidRegion selection = new CuboidRegion(pos1, pos2);
        long totalChange = selection.getVolume();
        AtomicLong progressChange = new AtomicLong(0);
        AtomicInteger delay = new AtomicInteger(1);
        for (BlockVector3 blockVector3 : selection) {
            Location blockLocation = new Location(world, blockVector3.getBlockX(), blockVector3.getBlockY(), blockVector3.getBlockZ());
            Bukkit.getRegionScheduler().runDelayed(plugin, blockLocation, scheduledTask -> {
                blockLocation.getBlock().setBiome(biome);
                if (progressChange.getAndIncrement() == totalChange) {
                    completableFuture.complete(true);
                }
            }, delay.getAndIncrement());
        }
        return completableFuture;
    }

    private static List<CuboidRegion> getMiniCuboidRegions(World world, org.bukkit.World w, Island island, int regionSize) {
        if (world == null) {
            world = BukkitAdapter.adapt(w);
        }

        Vector vmin = RegionUtils.getMinXRegion(w, island.getPosition().x(), island.getPosition().z());
        Vector vmax = RegionUtils.getMaxXRegion(w, island.getPosition().x(), island.getPosition().z());

        int minX = (int) vmin.getX();
        int minY = (int) vmin.getY();
        int minZ = (int) vmin.getZ();
        int maxX = (int) vmax.getX();
        int maxY = (int) vmax.getY();
        int maxZ = (int) vmax.getZ();

        List<CuboidRegion> miniRegions = new ArrayList<>();

        for (int x = minX; x < maxX; x += regionSize) {
            for (int z = minZ; z < maxZ; z += regionSize) {
                int endX = Math.min(x + (regionSize - 1), maxX);
                int endZ = Math.min(z + (regionSize - 1), maxZ);

                BlockVector3 minRegion = BlockVector3.at(x, minY, z);
                BlockVector3 maxRegion = BlockVector3.at(endX, maxY, endZ);

                miniRegions.add(new CuboidRegion(world, minRegion, maxRegion));
            }
        }

        return miniRegions;
    }

    private static CuboidRegion getCuboidRegion(World world, org.bukkit.World w, Island island) {
        if (world == null) {
            world = BukkitAdapter.adapt(w);
        }

        Vector vmin = RegionUtils.getMinXRegion(w, island.getPosition().x(), island.getPosition().z());
        Vector vmax = RegionUtils.getMaxXRegion(w, island.getPosition().x(), island.getPosition().z());

        BlockVector3 minRegion = BlockVector3.at(vmin.getX(), vmin.getY(), vmin.getZ());
        BlockVector3 maxRegion = BlockVector3.at(vmax.getX(), vmax.getY(), vmax.getZ());

        return new CuboidRegion(world, minRegion, maxRegion);
    }

    private static CuboidRegion getCuboidRegionWithRayon(World world, org.bukkit.World w, Island island, double rayon) {
        if (world == null) {
            world = BukkitAdapter.adapt(w);
        }
        Location center = RegionUtils.getCenterRegion(w, island.getPosition().x(), island.getPosition().z());
        BlockVector3 minRegion = BlockVector3.at(center.getBlockX() - rayon, world.getMinY(), center.getBlockZ() + rayon);
        BlockVector3 maxRegion = BlockVector3.at(center.getBlockX() + rayon, world.getMaxY(), center.getBlockZ() - rayon);

        return new CuboidRegion(world, minRegion, maxRegion);
    }

    public enum Type {
        WORLD_EDIT, FAST_ASYNC_WORLD_EDIT, UNDEFINED
    }
}
