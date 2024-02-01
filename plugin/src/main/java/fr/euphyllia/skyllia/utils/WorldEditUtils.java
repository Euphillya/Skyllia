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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashMap;
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
        RegionUtils.spiralStartCenter(position, island.getSize(), chunKPosition -> {
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

    public enum Type {
        WORLD_EDIT, FAST_ASYNC_WORLD_EDIT, UNDEFINED
    }
}
