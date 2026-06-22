package fr.euphyllia.skyllia.hook.fastasyncworldedit;

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
import com.sk89q.worldedit.util.SideEffectSet;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.hooks.SchematicHook;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicSetting;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;

public class FAWESchematicHook implements SchematicHook {

    private static final LinkedHashMap<File, ClipboardFormat> cache = new LinkedHashMap<>();
    private static final Logger logger = LogManager.getLogger(FAWESchematicHook.class);

    private final Plugin plugin;

    public FAWESchematicHook() {
        this.plugin = SkylliaAPI.getPlugin();
    }

    @Override
    public String name() {
        return "FastAsyncWorldEdit";
    }

    @Override
    public boolean isAvailable() {
        return SchematicHook.hasClass("com.fastasyncworldedit.core.FaweAPI")
                && Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null;
    }

    @Override
    public CompletableFuture<Boolean> paste(@NotNull Location loc, @NotNull SchematicSetting settings) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Bukkit.getAsyncScheduler().runNow(plugin, task -> {
            try {
                File file = new File(plugin.getDataFolder() + File.separator + settings.schematicFile());
                ClipboardFormat format = cache.computeIfAbsent(file, ClipboardFormats::findByFile);
                try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                    Clipboard clipboard = reader.read();
                    com.sk89q.worldedit.world.World w = BukkitAdapter.adapt(loc.getWorld());
                    try (EditSession editSession = WorldEdit.getInstance().newEditSession(w)) {
                        editSession.setSideEffectApplier(SideEffectSet.defaults());
                        editSession.setReorderMode(EditSession.ReorderMode.FAST);
                        Operation operation = new ClipboardHolder(clipboard)
                                .createPaste(editSession)
                                .to(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()))
                                .copyEntities(settings.copyEntities())
                                .ignoreAirBlocks(settings.ignoreAirBlocks())
                                .build();
                        Operations.complete(operation);
                    }
                }
                future.complete(true);
            } catch (Exception e) {
                logger.log(Level.FATAL, e.getMessage(), e);
                future.complete(false);
            }
        });
        return future;
    }
}
