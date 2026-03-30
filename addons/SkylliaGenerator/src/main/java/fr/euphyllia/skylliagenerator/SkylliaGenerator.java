package fr.euphyllia.skylliagenerator;

import fr.euphyllia.skylliagenerator.registry.GeneratorRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SkylliaGenerator extends JavaPlugin {

    private static final Logger log = LoggerFactory.getLogger(SkylliaGenerator.class);
    private static SkylliaGenerator instance;
    private GeneratorRegistry registry;

    public static SkylliaGenerator getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        if (Bukkit.getPluginManager().getPlugin("Skyllia") == null) {
            log.error("Skyllia is not installed!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        registry = new GeneratorRegistry();
    }
}
