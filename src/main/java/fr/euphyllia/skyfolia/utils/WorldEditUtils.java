package fr.euphyllia.skyfolia.utils;

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
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import fr.euphyllia.skyfolia.api.InterneAPI;
import fr.euphyllia.skyfolia.api.skyblock.model.IslandType;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;

public class WorldEditUtils {

    private static final LinkedHashMap<File, ClipboardFormat> cachedIslandSchematic = new LinkedHashMap<>();
    private static final Logger logger = LogManager.getLogger("fr.euphyllia.skyfolia.utils.WorldEditUtils");

    public static CompletableFuture<Boolean> pasteSchematicWE(InterneAPI api, Location loc, IslandType islandType) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
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
                        // configure here
                        .build();
                cachedIslandSchematic.putIfAbsent(file, format);
                Operations.complete(operation);
                completableFuture.complete(true);
            }
        }
        catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage());
            completableFuture.complete(false);
        }
        return completableFuture;
    }

}
