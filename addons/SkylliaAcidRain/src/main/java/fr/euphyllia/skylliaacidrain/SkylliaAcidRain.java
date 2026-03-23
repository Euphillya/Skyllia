package fr.euphyllia.skylliaacidrain;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SkylliaAcidRain extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        Bukkit.getAsyncScheduler().cancelTasks(this);
        Bukkit.getGlobalRegionScheduler().cancelTasks(this);
    }
}
