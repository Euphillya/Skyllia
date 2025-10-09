package fr.euphyllia.skyllia.hook.fastasyncworldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.biome.BiomeType;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.event.IslandBiomeChangeProgressEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicSetting;
import fr.euphyllia.skyllia.api.utils.RegionUtils;
import fr.euphyllia.skyllia.api.world.WorldModifier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public record FastAsyncWorldEditUtils(JavaPlugin plugin) implements WorldModifier {

    private static final LinkedHashMap<File, ClipboardFormat> cachedIslandSchematic = new LinkedHashMap<>();
    private static final Logger logger = LogManager.getLogger(FastAsyncWorldEditUtils.class);

    @Override
    public void pasteSchematicWE(@NotNull Location loc, @NotNull SchematicSetting settings) {
        Bukkit.getAsyncScheduler().runNow(plugin, task -> {
            try {
                File file = new File(plugin.getDataFolder() + File.separator + settings.schematicFile());
                ClipboardFormat format = cachedIslandSchematic.getOrDefault(file, ClipboardFormats.findByFile(file));
                cachedIslandSchematic.putIfAbsent(file, format);
                try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                    Clipboard clipboard = reader.read();
                    com.sk89q.worldedit.world.World w = BukkitAdapter.adapt(loc.getWorld());
                    try (EditSession editSession = WorldEdit.getInstance().newEditSession(w)) {
                        editSession.setSideEffectApplier(SideEffectSet.defaults());
                        editSession.setReorderMode(EditSession.ReorderMode.FAST);
                        Operation operation = new ClipboardHolder(clipboard)
                                .createPaste(editSession)
                                .to(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()))
                                .copyEntities(settings.copyEntities()) // Si la schem a des entités
                                .ignoreAirBlocks(settings.ignoreAirBlocks()) // On ne colle pas les blocks d'air de la schematic, gain de performance accru
                                .build();
                        Operations.complete(operation);
                    }
                }
            } catch (Exception e) {
                logger.log(Level.FATAL, e.getMessage(), e);
            }
        });
    }

    @Override
    public void deleteIsland(@NotNull Island island, @NotNull World w, int regionDistance, Consumer<Boolean> onFinish) {
        if (w == null) {
            if (onFinish != null) onFinish.accept(false);
            return;
        }

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
            Bukkit.getRegionScheduler().runDelayed(plugin, w, chunkPos.x(), chunkPos.z(), task -> {
                try {
                    SkylliaAPI.getWorldNMS().resetChunk(w, chunkPos);
                } catch (Exception e) {
                    failed.set(true);
                }
                if (toDelete.decrementAndGet() == 0 && onFinish != null) {
                    onFinish.accept(!failed.get());
                }
            }, delay.getAndIncrement());
        }
    }

    @Override
    public CompletableFuture<Boolean> changeBiomeChunk(@NotNull Location location, @NotNull Biome biome) {
        return changeBiomeChunk(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, biome);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<Boolean> changeBiomeChunk(@NotNull World world, int chunkX, int chunkZ, @NotNull Biome biome) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Bukkit.getRegionScheduler().execute(plugin, world, chunkX, chunkZ, () -> {
            com.sk89q.worldedit.world.World weWorld = new BukkitWorld(world);
            future.complete(SkylliaAPI.getBiomesImpl().setBiome(world, chunkX, chunkZ, biome));
            WorldEditPlugin.getInstance().getBukkitImplAdapter().sendBiomeUpdates(world, List.of(BlockVector2.at(chunkX, chunkZ)));
        });

        return future;
    }

    @Override
    public CompletableFuture<Boolean> changeBiomeIsland(@NotNull World world, @NotNull Biome biome, @NotNull Island island, int regionDistance) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

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

        RegionUtils.spiralStartCenter(island.getPosition(), regionDistance, radiusInBlocks, chunkPos ->
                affectedChunks.add(BlockVector2.at(chunkPos.x(), chunkPos.z()))
        );

        final int total = affectedChunks.size();

        processChunksSequentially(world, biome, island, affectedChunks, 0, total, future);
        return future;
    }

    private void processChunksSequentially(World world, Biome biome, Island island,
                                           List<BlockVector2> chunks, int index, int total,
                                           CompletableFuture<Boolean> future) {
        if (index >= chunks.size()) {
            future.complete(true);
            return;
        }

        BlockVector2 chunk = chunks.get(index);
        int chunkX = chunk.x();
        int chunkZ = chunk.z();

        changeBiomeChunk(world, chunkX, chunkZ, biome).whenComplete((success, throwable) -> {
            if (throwable != null || !success) {
                future.completeExceptionally(throwable != null ? throwable : new RuntimeException("Biome change failed at chunk " + chunkX + "," + chunkZ));
                return;
            }

            int left = total - index - 1;
            new IslandBiomeChangeProgressEvent(island, left, total).callEvent();

            processChunksSequentially(world, biome, island, chunks, index + 1, total, future);
        });
    }
}
