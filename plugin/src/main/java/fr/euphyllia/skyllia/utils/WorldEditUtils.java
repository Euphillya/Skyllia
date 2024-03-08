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
import fr.euphyllia.energie.model.MultipleRecords;
import fr.euphyllia.energie.model.SchedulerType;
import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicSetting;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.ConfigToml;
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

    public static void pasteSchematicWE(InterneAPI api, Location loc, SchematicSetting schematicWorld) {
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
        AtomicInteger chunkDeleted = new AtomicInteger(0);
        AtomicInteger numberChunkInIsland = new AtomicInteger(RegionHelper.getNumberChunkTotalInPerimeter((int) island.getSize() + 16)); // add secure distance 1 chunk
        AtomicInteger delay = new AtomicInteger(1);
        boolean deleteChunkPerimeterIsland = ConfigToml.deleteChunkPerimeterIsland;
        RegionUtils.spiralStartCenter(position, island.getSize(), chunKPosition -> {
            if (deleteChunkPerimeterIsland && chunkDeleted.getAndAdd(1) >= numberChunkInIsland.get()) {
                return;
            }
            SkylliaAPI.getScheduler()
                    .runDelayed(SchedulerType.SYNC, new MultipleRecords.WorldChunk(w, chunKPosition.x(), chunKPosition.z()), t -> {
                        plugin.getInterneAPI().getWorldNMS().resetChunk(w, chunKPosition);
                    }, delay.getAndIncrement());
        });
    }

    public static CompletableFuture<Boolean> changeBiomeChunk(Main plugin, org.bukkit.World world, Biome biome, Island island) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        if (world == null) {
            throw new RuntimeException("World is not loaded or not exist");
        }
        Position position = island.getPosition();
        AtomicInteger delay = new AtomicInteger(1);
        AtomicInteger numberChunkInIsland = new AtomicInteger(RegionHelper.getNumberChunkTotalInPerimeter((int) island.getSize() + 16)); // add secure distance 1 chunk
        AtomicInteger chunkModified = new AtomicInteger(0);
        try {
            RegionUtils.spiralStartCenter(position, island.getSize(), chunKPosition -> {
                if (chunkModified.getAndAdd(1) >= numberChunkInIsland.get()) {
                    completableFuture.complete(true);
                    return;
                }
                SkylliaAPI.getScheduler()
                        .runDelayed(SchedulerType.SYNC, new MultipleRecords.WorldChunk(world, chunKPosition.x(), chunKPosition.z()), t -> {
                            for (int x = 0; x < 16; x++) {
                                for (int z = 0; z < 16; z++) {
                                    for (int y = 0; y < world.getMaxHeight(); y++) {
                                        Location blockLocation = new Location(world, (chunKPosition.x() << 4) + x, y, (chunKPosition.z() << 4) + z);
                                        blockLocation.getBlock().setBiome(biome);
                                    }
                                }
                            }
                        }, delay.getAndIncrement());
            });
        } finally {
            completableFuture.complete(true);
        }
        return completableFuture;
    }

    public enum Type {
        WORLD_EDIT, FAST_ASYNC_WORLD_EDIT, UNDEFINED
    }
}
