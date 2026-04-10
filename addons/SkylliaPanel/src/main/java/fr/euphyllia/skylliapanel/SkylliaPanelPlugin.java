package fr.euphyllia.skylliapanel;

import dev.triumphteam.gui.TriumphGui;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skylliapanel.command.PanelCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class SkylliaPanelPlugin extends JavaPlugin {

    private static SkylliaPanelPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        TriumphGui.init(this);

        if (!getDataFolder().exists()) getDataFolder().mkdirs();

        PanelCommand panelCommand = new PanelCommand(this);
        SkylliaAPI.registerCommands(panelCommand, "panel");
    }

    @Override
    public void onDisable() {

    }

    public static SkylliaPanelPlugin getInstance() {
        return instance;
    }
}
