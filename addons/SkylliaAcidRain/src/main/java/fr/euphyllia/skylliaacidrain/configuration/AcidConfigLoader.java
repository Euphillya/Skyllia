package fr.euphyllia.skylliaacidrain.configuration;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skylliaacidrain.listener.AcidListener;

import java.io.File;

public class AcidConfigLoader {

    public static AcidConfigManager config;

    public static void init(File dataFolder, AcidListener listener) throws Exception {
        File configDir = new File(dataFolder, "config");
        //noinspection ResultOfMethodCallIgnored
        configDir.mkdirs();
        config = new AcidConfigManager(loadFile(new File(configDir, "config.toml")), listener);

        config.loadConfig();

        SkylliaAPI.getConfigRegistry().registerConfig(config);
    }

    public static void unregister() {
        if (config != null) SkylliaAPI.getConfigRegistry().unregisterConfig(config);
    }

    private static CommentedFileConfig loadFile(File file) {
        CommentedFileConfig cfg = CommentedFileConfig.builder(file).sync().autosave().build();
        cfg.load();
        return cfg;
    }
}
