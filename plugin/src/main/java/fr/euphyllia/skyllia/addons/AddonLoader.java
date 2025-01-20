package fr.euphyllia.skyllia.addons;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.addons.AddonLoadPhase;
import fr.euphyllia.skyllia.api.addons.SkylliaAddon;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * This class scans and loads addons from the /addons folder.
 */
public class AddonLoader {

    private static final List<SkylliaAddon> LOADED_ADDONS = new ArrayList<>();

    private final Main plugin;
    private final Logger logger;

    /**
     * Constructs an AddonLoader.
     *
     * @param plugin the main plugin instance
     * @param logger a shared logger
     */
    public AddonLoader(Main plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    /**
     * Searches for .jar files in the addons directory and loads them based on the specified phase.
     *
     * @param phase the AddonLoadPhase (BEFORE or AFTER plugin enable)
     */
    public void loadAddons(AddonLoadPhase phase) {
        File extensionsDir = new File(plugin.getDataFolder(), "addons");
        if (!extensionsDir.exists()) {
            extensionsDir.mkdirs();
            logger.info("Addon directory created at {}", extensionsDir.getAbsolutePath());
            return;
        }

        File[] files = extensionsDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (files == null || files.length == 0) {
            logger.warn("No addon files found in {}", extensionsDir.getAbsolutePath());
            return;
        }

        for (File file : files) {
            try {
                URL jarUrl = file.toURI().toURL();
                URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl}, plugin.getClass().getClassLoader());

                ServiceLoader<SkylliaAddon> serviceLoader = ServiceLoader.load(SkylliaAddon.class, classLoader);
                for (SkylliaAddon addon : serviceLoader) {
                    if (addon.getLoadPhase() == phase) {
                        addon.onLoad(plugin);
                        addon.onEnable();
                        LOADED_ADDONS.add(addon);
                        logger.info("Loaded {} addon: {}", phase, addon.getClass().getName());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to load addon: {}", file.getName(), e);
            }
        }
    }

    /**
     * Disables all loaded addons, clearing the internal list.
     */
    public static void disableAllAddons() {
        for (SkylliaAddon addon : LOADED_ADDONS) {
            try {
                addon.onDisabled();
            } catch (Exception ignored) {
            }
        }
        LOADED_ADDONS.clear();
    }
}
