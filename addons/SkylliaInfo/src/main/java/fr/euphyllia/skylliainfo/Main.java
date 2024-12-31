package fr.euphyllia.skylliainfo;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandRegistry;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

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
