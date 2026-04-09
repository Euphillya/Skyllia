package fr.euphyllia.skylliapanel;

import dev.triumphteam.gui.TriumphGui;
import org.bukkit.plugin.java.JavaPlugin;

public class SkylliaPanelPlugin extends JavaPlugin {

    private static SkylliaPanelPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        TriumphGui.init(this);

    }

    @Override
    public void onDisable() {

    }
}
