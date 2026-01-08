package fr.euphyllia.skyllia.hook.luminol;

import fr.euphyllia.skyllia.api.hooks.ServerHook;
import fr.euphyllia.skyllia.hook.luminol.teleport.PlayerTeleportHooks;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuminolHook implements ServerHook {
    private static final Logger log = LoggerFactory.getLogger(LuminolHook.class);

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
        log.warn("Luminol can not support cancellable teleport events. Some features may not work as expected.");
        manager.registerEvents(new PlayerTeleportHooks(), skylliaPlugin);
    }
}
