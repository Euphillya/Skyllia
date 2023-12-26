package fr.euphyllia.skyfolia.utils;

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
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockState;
import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.InterneAPI;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.model.IslandType;
import fr.euphyllia.skyfolia.api.skyblock.model.Position;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WorldEditUtils {

    private static final LinkedHashMap<File, ClipboardFormat> cachedIslandSchematic = new LinkedHashMap<>();
    private static final Logger logger = LogManager.getLogger(WorldEditUtils.class);

    public static Type worldEditVersion() {
        if (Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit")){
            return Type.FAST_ASYNC_WORLD_EDIT;
        } else if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit")){
            return Type.WORLD_EDIT;
        }
        return Type.UNDEFINED;
    }

    public static void pasteSchematicWE(InterneAPI api, Location loc, IslandType islandType) {
        try {
            File file = new File(api.getPlugin().getDataFolder() + File.separator + islandType.schematic());
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
        }
        catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage());
        }
    }

    public static void deleteIsland(Main plugin, Island island, org.bukkit.World w, int rayon) {
        if (w == null) {
            throw new RuntimeException("World is not loaded or not exist");
        }

        switch (worldEditVersion()) {
            case FAST_ASYNC_WORLD_EDIT -> {
                Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
                    World world = BukkitAdapter.adapt(w);
                    CuboidRegion selection = getCuboidRegionWithRayon(world, w, island, rayon);
                    try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(world).maxBlocks(-1).build();) { // get the edit session and use -1 for max blocks for no limit, this is a try with resources statement to ensure the edit session is closed after use
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
                List<Block> blocksList = new ArrayList<>();

                selection.forEach(blockVector3 -> blocksList.add(new Location(w, blockVector3.getBlockX(), blockVector3.getBlockY(), blockVector3.getBlockZ()).getBlock()));

                for (Block block : blocksList) {
                    Bukkit.getRegionScheduler().run(plugin, w, block.getChunk().getX(), block.getChunk().getZ(), t -> {
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

    public static void changeBiome(Main plugin, Island island, org.bukkit.World world, Biome biome, Player player) {
        if (player.getWorld().getUID() != world.getUID()) return;
        BiomeType biomeType = BiomeType.REGISTRY.get(biome.name().toLowerCase());
        if (biomeType == null) {
            logger.log(Level.FATAL, "BIOME NON TROUVER");
            return;
        }
        CuboidRegion selection = getCuboidRegion(null, world, island);
        List<Block> blocksList = new ArrayList<>();

        selection.forEach(blockVector3 -> blocksList.add(new Location(world, blockVector3.getBlockX(), blockVector3.getBlockY(), blockVector3.getBlockZ()).getBlock()));

        ConcurrentHashMap<Position, Location> positions = new ConcurrentHashMap<>();
        AtomicInteger i = new AtomicInteger(1);
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        for (Block block : blocksList) {
            BlockVector3 blockBiomeSet = BlockVector3.at(block.getX(), block.getY(), block.getZ());
            if (worldEditVersion().equals(Type.FAST_ASYNC_WORLD_EDIT)) {
                positions.putIfAbsent(new Position(block.getChunk().getX(), block.getChunk().getZ()), block.getLocation());
                biomeType.applyBiome(blockBiomeSet);
            } else if (worldEditVersion().equals(Type.WORLD_EDIT)) {
                executorService.schedule(() -> {
                    Bukkit.getServer().getRegionScheduler().run(plugin, block.getLocation(), t ->{
                        logger.log(Level.FATAL, "Insertion " + block.getLocation().toString());
                        block.setType(Material.END_STONE);
                    });
                }, i.get(), TimeUnit.SECONDS);
            } else {
                throw new RuntimeException("hein ?!");
            }
            i.set(i.get() + 1);
        }
        executorService.shutdown();
        logger.log(Level.INFO, "fini ?");
        positions.forEach((position, locTemp) -> {
            Bukkit.getServer().getRegionScheduler().run(plugin, locTemp, scheduledTask -> {
                PlayerUtils.refreshPlayerChunk(player, locTemp.getChunk().getX(), locTemp.getChunk().getZ());
            });
        });
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

    private static CuboidRegion getCuboidRegionWithRayon(World world, org.bukkit.World w, Island island, int rayon) {
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
