package fr.euphyllia.skyllia.hook.canvas;

import fr.euphyllia.skyllia.api.hooks.ServerHook;
import fr.euphyllia.skyllia.hook.canvas.teleport.PlayerTeleportHooks;
import org.bukkit.plugin.Plugin;

public class CanvasHook implements ServerHook {

    @Override
    public String name() {
        return "Canvas";
    }

    @Override
    public boolean isAvailable() {
        return hasClass("io.canvasmc.canvas.Config");
    }

    @Override
    public void register(Plugin skylliaPlugin) {
        var manager = skylliaPlugin.getServer().getPluginManager();
        manager.registerEvents(new PlayerTeleportHooks(), skylliaPlugin);
    }
}
