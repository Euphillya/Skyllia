package fr.euphyllia.skyllia.configuration.manager;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.managers.ConfigManager;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LanguageConfigManager implements ConfigManager {

    private static final Logger log = LogManager.getLogger(LanguageConfigManager.class);
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<Locale, Map<String, String>> translations = new HashMap<>();
    private final Locale defaultLocale = Locale.of("en", "GB");
    private final Skyllia plugin = Skyllia.getPlugin(Skyllia.class);

    @Override
    public void loadConfig() throws DatabaseException {
        File langDir = new File(plugin.getDataFolder(), "language");
        if (!langDir.exists()) langDir.mkdirs();
        File[] files = langDir.listFiles((dir, name) -> name.endsWith(".toml"));
        if (files == null || files.length == 0) {
            throw new IllegalStateException("No language files were found in the 'language' directory. Expected at least one '.toml' file (e.g., 'en_GB.toml').");
        }

        translations.clear();

        for (File file : files) {
            Locale locale = parseLocale(file.getName());
            CommentedFileConfig tomlConfig = CommentedFileConfig.builder(file).sync().autosave().build();
            tomlConfig.load();

            Map<String, String> messages = new HashMap<>();
            parseConfig("", tomlConfig, messages);
            translations.put(locale, messages);

            log.info("Loaded language file: {} ({} keys)", file.getName(), messages.size());
        }

        if (!translations.containsKey(defaultLocale)) {
            throw new IllegalStateException(
                    "Default language file not loaded. Expected a file named '"
                            + defaultLocale.toLanguageTag().replace("-", "_")
                            + ".toml' (e.g., 'en_GB.toml') in the 'language' folder, but it was not found or could not be parsed correctly."
            );
        }

    }

    private Locale parseLocale(String filename) {
        String baseName = filename.replace(".toml", "");
        String[] parts = baseName.split("_");
        if (parts.length == 2) {
            return Locale.of(parts[0], parts[1]);
        }
        return defaultLocale;
    }

    private void parseConfig(String prefix, CommentedConfig config, Map<String, String> messages) {
        for (String key : config.valueMap().keySet()) {
            Object value = config.get(key);
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            if (value instanceof CommentedConfig subConfig) {
                parseConfig(fullKey, subConfig, messages);
            } else {
                messages.put(fullKey, String.valueOf(value));
            }
        }
    }

    public Component translate(Locale locale, String key, Map<String, String> placeholders) {
        Map<String, String> langMessages = translations.get(locale);
        if (langMessages == null) {
            langMessages = translations.get(defaultLocale);
            if (langMessages == null) {
                throw new IllegalStateException("No translations found for locale: " + locale + " or default locale.");
            }
        }
        String message = langMessages.getOrDefault(key, "<red>Missing translation: " + key);

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        String prefix = langMessages.getOrDefault("prefix", "<light_purple>[Skyllia]</light_purple> :");

        return miniMessage.deserialize(prefix + message);
    }

    public Component translate(String key, Map<String, String> placeholders) {
        return translate(defaultLocale, key, placeholders);
    }

    public Component translate(Player player, String key, Map<String, String> placeholders) {
        return translate(player.locale(), key, placeholders);
    }

    public void sendMessage(Player player, String key, Map<String, String> placeholders) {
        player.sendMessage(translate(player, key, placeholders));
    }

    public void sendMessage(CommandSender sender, String key, Map<String, String> placeholders) {
        sender.sendMessage(translate(key, placeholders));
    }

    public void sendMessage(Player player, String key) {
        player.sendMessage(translate(player, key, Map.of()));
    }

    public void sendMessage(CommandSender sender, String key) {
        sender.sendMessage(translate(key, Map.of()));
    }

}
