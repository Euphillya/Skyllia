package fr.euphyllia.skyllia.utils;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.event.IslandBiomeChangeProgressEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicSetting;
import fr.euphyllia.skyllia.api.utils.RegionUtils;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
        AtomicInteger numberChunkInIsland = new AtomicInteger(RegionHelper.getTotalChunksInBlockPerimeter((int) island.getSize() + 32)); // add secure distance 2 chunk
        AtomicInteger delay = new AtomicInteger(1);
        boolean deleteChunkPerimeterIsland = ConfigLoader.general.isDeleteChunkPerimeterIsland();
        RegionUtils.spiralStartCenter(position, ConfigLoader.general.getRegionDistance(), island.getSize(), chunKPosition -> {
            if (deleteChunkPerimeterIsland && chunkDeleted.getAndAdd(2) >= numberChunkInIsland.get()) {
                return;
            }
            Bukkit.getRegionScheduler().runDelayed(plugin, w, chunKPosition.x(), chunKPosition.z(), task ->
                    plugin.getInterneAPI().getWorldNMS().resetChunk(w, chunKPosition), Math.max(1, delay.getAndIncrement()));
        });
    }

    public static CompletableFuture<Boolean> changeBiomeChunk(org.bukkit.Chunk chunk, Biome biome) {
        return changeBiomeChunk(chunk.getWorld(), chunk.getX(), chunk.getZ(), biome);
    }
    public static CompletableFuture<Boolean> changeBiomeChunk(org.bukkit.World world, int chunkX, int chunkZ, Biome biome) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        int minHeight = world.getMinHeight();
        int maxHeight = world.getMaxHeight();
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;

        String biomeKey = biome.getKey().getKey().toLowerCase();
        Bukkit.getRegionScheduler().execute(Main.getPlugin(Main.class), world, chunkX, chunkZ, () -> {

            com.sk89q.worldedit.world.World weWorld = new BukkitWorld(world);
            BiomeType biomeType = BiomeType.REGISTRY.get(biomeKey);

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = minHeight; y < maxHeight; y++) {
                        weWorld.setBiome(BlockVector3.at(baseX + x, y, baseZ + z), biomeType);
                    }
                }
            }
            future.complete(true);
            weWorld.sendBiomeUpdates(List.of(BlockVector2.at(chunkX, chunkZ)));
        });

        return future;
    }

    public static CompletableFuture<Boolean> changeBiomeIsland(org.bukkit.World world, Biome biome, Island island) {
        CompletableFuture<Boolean> future  = new CompletableFuture<>();
        if (world == null) {
            future.completeExceptionally(new RuntimeException("World is not loaded or doesn't exist"));
            return future;
        }

        BiomeType biomeType = BiomeType.REGISTRY.get(biome.getKey().getKey().toLowerCase());
        if (biomeType == null) {
            future.completeExceptionally(new RuntimeException("Invalid biome: " + biome.translationKey()));
            return future;
        }

        int radiusInBlocks = (int) Math.ceil(island.getSize()) + 16;

        List<BlockVector2> affectedChunks = new ArrayList<>();

        RegionUtils.spiralStartCenter(island.getPosition(), ConfigLoader.general.getRegionDistance(), radiusInBlocks, chunkPos -> {
            affectedChunks.add(BlockVector2.at(chunkPos.x(), chunkPos.z()));
        });

        AtomicInteger remaining = new AtomicInteger(affectedChunks.size());

        for (BlockVector2 chunkVec : affectedChunks) {
            int chunkX = chunkVec.x();
            int chunkZ = chunkVec.z();
            try {
                if (changeBiomeChunk(world, chunkX, chunkZ, biome).join()) {
                    int left = remaining.decrementAndGet();
                    new IslandBiomeChangeProgressEvent(island, left, affectedChunks.size()).callEvent();
                    if (left == 0) {
                        future.complete(true);
                        break;
                    }
                }
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }
        return future;
    }

    public enum Type {
        WORLD_EDIT, FAST_ASYNC_WORLD_EDIT, UNDEFINED
    }
}
