package fr.euphyllia.skyfolia;


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private final Logger logger = LogManager.getLogger(this);

    @Override
    public void onEnable() {
        logger.log(Level.INFO, "Plugin Start");
    }
}