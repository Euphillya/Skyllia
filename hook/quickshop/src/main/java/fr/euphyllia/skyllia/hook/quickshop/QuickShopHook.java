package fr.euphyllia.skyllia.hook.quickshop;

import fr.euphyllia.skyllia.api.hooks.PluginHook;
import fr.euphyllia.skyllia.hook.quickshop.configuration.QSConfigLoader;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuickShopHook implements PluginHook {

    private static final Logger log = LoggerFactory.getLogger(QuickShopHook.class);

    private static final boolean CLASS_AVAILABLE =
            PluginHook.hasClass("com.ghostchu.quickshop.api.event.management.ShopCreateEvent");

    @Override
    public String name() {
        return "QuickShop-Hikari";
    }

    @Override
    public boolean isAvailable() {
        return CLASS_AVAILABLE && Bukkit.getPluginManager().getPlugin("QuickShop-Hikari") != null;
    }

    @Override
    public void register(Plugin skylliaPlugin) {
        try {
            QSConfigLoader.init(skylliaPlugin.getDataFolder());
            log.debug("Hook QuickShop-Hikari enabled");
        } catch (Exception e) {
            log.error("Failed to load QuickShop hook configuration — hook will not be registered", e);
            return;
        }

        skylliaPlugin.getServer().getPluginManager()
                .registerEvents(new QuickShopListener(), skylliaPlugin);
    }

    @Override
    public void unregister() {
        QSConfigLoader.unregister();
    }
}
