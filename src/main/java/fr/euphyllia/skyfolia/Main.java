package fr.euphyllia.skyfolia;


import fr.euphyllia.skyfolia.api.InterneAPI;
import fr.euphyllia.skyfolia.managers.Managers;
import fr.euphyllia.skyfolia.utils.exception.DatabaseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class Main extends JavaPlugin {

    private final Logger logger = LogManager.getLogger(this);
    private InterneAPI interneAPI;

    @Override
    public void onEnable() {
        logger.log(Level.INFO, "Plugin Start");
        this.interneAPI = new InterneAPI(this);
        try {
            if (!this.interneAPI.setupConfigs(this.getDataFolder(), "config.toml")) {
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
            if (!this.interneAPI.setupSGBD()) {
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        } catch (DatabaseException | IOException exception) {
            this.logger.log(Level.FATAL, exception.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.interneAPI.setManagers(new Managers(interneAPI));
        this.interneAPI.getManagers().init();
    }

    @Override
    public void onDisable() {
        this.logger.log(Level.INFO, "Plugin Off");
        if (this.interneAPI.getDatabaseLoader() != null) {
            this.interneAPI.getDatabaseLoader().closeDatabase();
        }
    }

    public InterneAPI getInterneAPI() {
        return this.interneAPI;
    }
}