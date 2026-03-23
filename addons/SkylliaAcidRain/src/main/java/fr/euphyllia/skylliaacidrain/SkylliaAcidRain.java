package fr.euphyllia.skylliaacidrain;

import fr.euphyllia.skylliaacidrain.configuration.AcidConfigLoader;
import fr.euphyllia.skylliaacidrain.listener.AcidListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SkylliaAcidRain extends JavaPlugin {


    private static final Logger log = LoggerFactory.getLogger(SkylliaAcidRain.class);

    @Override
    public void onEnable() {

        AcidListener acidListener = new AcidListener(this);

        try {
            AcidConfigLoader.init(getDataFolder(), acidListener);
        } catch (Exception e) {
            log.error("Error while loading AcidConfig", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(acidListener, this);
    }

    @Override
    public void onDisable() {
        AcidConfigLoader.unregister();
        Bukkit.getAsyncScheduler().cancelTasks(this);
        Bukkit.getGlobalRegionScheduler().cancelTasks(this);
    }
}
