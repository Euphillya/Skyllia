package fr.euphyllia.skyllia.utils;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicWorld;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
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
            logger.log(Level.FATAL, e.getMessage());
        }
    }

    public static void deleteIsland(Main plugin, Island island, org.bukkit.World w, double rayon) {
        if (w == null) {
            throw new RuntimeException("World is not loaded or not exist");
        }

        switch (worldEditVersion()) {
            case FAST_ASYNC_WORLD_EDIT -> {
                Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
                    World world = BukkitAdapter.adapt(w);
                    CuboidRegion selection = getCuboidRegionWithRayon(world, w, island, rayon);
                    try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(world).maxBlocks(-1).build()) { // get the edit session and use -1 for max blocks for no limit, this is a try with resources statement to ensure the edit session is closed after use
                        RandomPattern pat = new RandomPattern(); // Create the random pattern
                        BlockState air = BukkitAdapter.adapt(Material.AIR.createBlockData());
                        pat.add(air, 1);
                        editSession.setReorderMode(EditSession.ReorderMode.MULTI_STAGE);
                        editSession.setSideEffectApplier(SideEffectSet.defaults());
                        editSession.setBlocks(selection, pat);
                    } catch (MaxChangedBlocksException ex) {
                        logger.log(Level.FATAL, ex);
                    }
                });
            }
            case WORLD_EDIT -> Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
                CuboidRegion selection = getCuboidRegionWithRayon(null, w, island, rayon);
                List<Location> blocksLocationList = new ArrayList<>();

                selection.forEach(blockVector3 -> blocksLocationList.add(new Location(w, blockVector3.getBlockX(), blockVector3.getBlockY(), blockVector3.getBlockZ())));

                for (Location blockLocation : blocksLocationList) {
                    Bukkit.getRegionScheduler().run(plugin, blockLocation, t -> {
                        Block block = blockLocation.getBlock();
                        if (block.getType().isAir()) return;
                        block.setType(Material.AIR, false);
                    });
                }
            });
            default -> {
                RegionUtils.editBlockRegion(w, island.getPosition().regionX(), island.getPosition().regionZ(), plugin, location -> {
                    Bukkit.getRegionScheduler().run(plugin, location, t1 -> {
                        Block block = location.getBlock();
                        if (block.getType().isAir()) return;
                        block.setType(Material.AIR);
                    });
                }, 20);
            }
        }
    }

    public static CompletableFuture<Boolean> changeBiomeChunk(Main plugin, org.bukkit.World world, Biome biome, Position position) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        int blockX = position.regionX() << 4;
        int blockZ = position.regionZ() << 4;
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

        Vector vmin = RegionUtils.getMinXRegion(w, island.getPosition().regionX(), island.getPosition().regionZ());
        Vector vmax = RegionUtils.getMaxXRegion(w, island.getPosition().regionX(), island.getPosition().regionZ());

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

        Vector vmin = RegionUtils.getMinXRegion(w, island.getPosition().regionX(), island.getPosition().regionZ());
        Vector vmax = RegionUtils.getMaxXRegion(w, island.getPosition().regionX(), island.getPosition().regionZ());

        BlockVector3 minRegion = BlockVector3.at(vmin.getX(), vmin.getY(), vmin.getZ());
        BlockVector3 maxRegion = BlockVector3.at(vmax.getX(), vmax.getY(), vmax.getZ());

        return new CuboidRegion(world, minRegion, maxRegion);
    }

    private static CuboidRegion getCuboidRegionWithRayon(World world, org.bukkit.World w, Island island, double rayon) {
        if (world == null) {
            world = BukkitAdapter.adapt(w);
        }
        Location center = RegionUtils.getCenterRegion(w, island.getPosition().regionX(), island.getPosition().regionZ());
        BlockVector3 minRegion = BlockVector3.at(center.getBlockX() - rayon, world.getMinY(), center.getBlockZ() + rayon);
        BlockVector3 maxRegion = BlockVector3.at(center.getBlockX() + rayon, world.getMaxY(), center.getBlockZ() - rayon);

        return new CuboidRegion(world, minRegion, maxRegion);
    }

    public enum Type {
        WORLD_EDIT, FAST_ASYNC_WORLD_EDIT, UNDEFINED
    }
}
