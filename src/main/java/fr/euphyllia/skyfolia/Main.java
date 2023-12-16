package fr.euphyllia.skyfolia;


import fr.euphyllia.skyfolia.api.InterneAPI;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private final Logger logger = LogManager.getLogger(this);
    private static InterneAPI interneAPI;

    @Override
    public void onEnable() {
        logger.log(Level.INFO, "Plugin Start");
        interneAPI = new InterneAPI(this);
        if (!interneAPI.setupConfigs("config.toml")) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (!interneAPI.setupSGBD()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
    }

    @Override
    public void onDisable() {
        logger.log(Level.INFO, "Plugin Off");
        if (interneAPI.getDatabaseLoader() != null) {
            interneAPI.getDatabaseLoader().closeDatabase();
        }
    }

    public static InterneAPI getInterneAPI() {
        return interneAPI;
    }
}