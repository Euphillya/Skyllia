package fr.euphyllia.skylliachest;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skylliachest.api.ChestIsland;
import fr.euphyllia.skylliachest.cache.ChestIslandCache;
import fr.euphyllia.skylliachest.commands.ChestCommand;
import fr.euphyllia.skylliachest.listeners.ChestListener;
import fr.euphyllia.skylliachest.manager.ChestManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

public class SkylliaChest extends JavaPlugin {

    private static final Logger log = LoggerFactory.getLogger(SkylliaChest.class);
    private static SkylliaChest instance;
    private ChestIslandCache chestCache;
    private ChestManager chestManager;

    public static SkylliaChest getInstance() {
        return instance;
    }


    @Override
    public void onEnable() {
        instance = this;

        chestCache = new ChestIslandCache();
        chestManager = new ChestManager(this);

        SkylliaAPI.registerCommands(new ChestCommand(this), "chest", "echest", "islandchest");

        getServer().getPluginManager().registerEvents(new ChestListener(this), this);

        // Annoncer que c'est en beta
        log.warn("SkylliaChest is currently in beta. Please report any issues you encounter to the developer.");
    }

    @Override
    public void onDisable() {
        if (chestCache != null && chestManager != null) {
            saveAllChests();
            chestCache.clear();
        }

        Bukkit.getAsyncScheduler().cancelTasks(this);
        Bukkit.getGlobalRegionScheduler().cancelTasks(this);
    }

    private void saveAllChests() {
        Map<UUID, ChestIsland> allChests = chestCache.getAllCachedChests();

        int saved = 0;
        int failed = 0;

        for (ChestIsland chestIsland : allChests.values()) {
            if (chestIsland.isDirty()) {
                boolean success = chestManager.saveChest(chestIsland);
                if (success) {
                    saved++;
                } else {
                    failed++;
                    log.error("Failed to save chest for island {}", chestIsland.getIsland().getId());
                }
            }
        }

        if (saved > 0) {
            log.info("Saved {} chest(s) to the database", saved);
        }

        if (failed > 0) {
            log.error("Failed to save {} chest(s) to the database", failed);
        }
    }

    public ChestIslandCache getChestCache() {
        return chestCache;
    }

    public ChestManager getChestManager() {
        return chestManager;
    }
}
