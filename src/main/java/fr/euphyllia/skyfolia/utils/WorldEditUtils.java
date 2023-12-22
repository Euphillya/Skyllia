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
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.InterneAPI;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.model.IslandType;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashMap;

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

    public static void deleteIsland(Main plugin, Island island, org.bukkit.World w, int t) {
        if (w == null) {
            throw new RuntimeException("World is not loaded or not exist");
        }

        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(w);

        Location center = RegionUtils.getCenterRegion(w, island.getPosition().regionX(), island.getPosition().regionZ());

        int size = 3;
        int rayon = 50;

        BlockVector3 minRegion = BlockVector3.at(center.getBlockX() - rayon - size, world.getMinY(), center.getBlockZ() + rayon + size);
        BlockVector3 maxRegion = BlockVector3.at(center.getBlockX() + rayon + size, world.getMaxY(), center.getBlockZ() - rayon - size);

        CuboidRegion selection = new CuboidRegion(world, minRegion, maxRegion);

        Bukkit.getServer().getRegionScheduler().run(plugin, center, scheduledTask -> {
            try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(world).maxBlocks(-1).build();) { // get the edit session and use -1 for max blocks for no limit, this is a try with resources statement to ensure the edit session is closed after use
                RandomPattern pat = new RandomPattern(); // Create the random pattern
                BlockState air = BukkitAdapter.adapt(Material.AIR.createBlockData());
                pat.add(air, 1);
                editSession.setReorderMode(EditSession.ReorderMode.MULTI_STAGE);
                editSession.setBlocks(selection, pat);
            } catch (MaxChangedBlocksException ex) {
                logger.log(Level.FATAL, ex);
            }
        });
    }

    public enum Type {
        WORLD_EDIT, FAST_ASYNC_WORLD_EDIT, UNDEFINED
    }
}
