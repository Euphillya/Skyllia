package fr.euphyllia.skyllia.cache;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;

import java.util.concurrent.TimeUnit;

/**
 * Schedules periodic cache updates.
 */
public class CacheScheduler {

    private final Main plugin;
    private final InterneAPI interneAPI;
    private final Logger logger;

    /**
     * Constructs a CacheScheduler.
     *
     * @param plugin     the main plugin instance
     * @param interneAPI the internal API
     * @param logger     a shared logger
     */
    public CacheScheduler(Main plugin, InterneAPI interneAPI, Logger logger) {
        this.plugin = plugin;
        this.interneAPI = interneAPI;
        this.logger = logger;
    }

    /**
     * Schedules the cache update task at a fixed rate defined in the config.
     */
    public void scheduleCacheUpdate() {
        Bukkit.getAsyncScheduler().runAtFixedRate(
                plugin,
                task -> Bukkit.getOnlinePlayers().forEach(interneAPI::updateCache),
                1,
                ConfigToml.updateCacheTimer,
                TimeUnit.SECONDS
        );
        logger.info("CacheScheduler started with an interval of {} seconds.", ConfigToml.updateCacheTimer);
    }
}
