package fr.euphyllia.skylliainfo;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import org.bukkit.plugin.java.JavaPlugin;

public final class SkylliaInfo extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        SkylliaAPI.registerCommands(new IslandInfoCommand(this), "info");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
