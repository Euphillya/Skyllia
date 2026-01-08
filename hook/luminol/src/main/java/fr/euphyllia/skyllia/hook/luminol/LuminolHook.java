package fr.euphyllia.skyllia.hook.luminol;

import fr.euphyllia.skyllia.api.hooks.ServerHook;
import fr.euphyllia.skyllia.hook.luminol.teleport.PlayerTeleportHooks;
import org.bukkit.plugin.Plugin;

public class LuminolHook implements ServerHook {
    @Override
    public String name() {
        return "Luminol";
    }

    @Override
    public boolean isAvailable() {
        return hasClass("me.earthme.luminol.config.ConfigManager");
    }

    @Override
    public void register(Plugin skylliaPlugin) {
        var manager = skylliaPlugin.getServer().getPluginManager();
        manager.registerEvents(new PlayerTeleportHooks(), skylliaPlugin);
    }
}
