package fr.euphyllia.skyllia.cache;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;

import java.util.concurrent.TimeUnit;

/**
 * Schedules periodic cache updates.
 */
public class CacheScheduler {

    private final Skyllia plugin;
    private final InterneAPI interneAPI;
    private final Logger logger = LogManager.getLogger(this);

    /**
     * @param plugin     the Skyllia plugin instance
     * @param interneAPI the internal API
     */
    public CacheScheduler(Skyllia plugin, InterneAPI interneAPI) {
        this.plugin = plugin;
        this.interneAPI = interneAPI;
    }

    /**
     * Schedules the cache update task at a fixed rate defined in the config.
     */
    public void scheduleCacheUpdate() {
        long interval = ConfigLoader.general.getUpdateCacheTimer();
        Bukkit.getAsyncScheduler().runAtFixedRate(
                plugin,
                task -> {
                    Bukkit.getOnlinePlayers().forEach(interneAPI::updateCache);
                },
                1,
                interval,
                TimeUnit.SECONDS
        );
        logger.info("CacheScheduler started with an interval of {} seconds.", interval);
    }
}