package fr.euphyllia.skyllia.hook.quickshop.configuration;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fr.euphyllia.skyllia.api.SkylliaAPI;

import java.io.File;

public class QSConfigLoader {

    public static QSConfigManager config;

    public static void init(File skylliaDataFolder) throws Exception {
        File addonsDir = new File(skylliaDataFolder, "addons");
        //noinspection ResultOfMethodCallIgnored
        addonsDir.mkdirs();

        config = new QSConfigManager(loadFile(new File(addonsDir, "quickshop.toml")));
        config.loadConfig();

        SkylliaAPI.getConfigRegistry().registerConfig(config);
    }

    public static void unregister() {
        if (config != null) {
            SkylliaAPI.getConfigRegistry().unregisterConfig(config);
        }
    }

    private static CommentedFileConfig loadFile(File file) {
        CommentedFileConfig cfg = CommentedFileConfig.builder(file).sync().autosave().build();
        cfg.load();
        return cfg;
    }
}
