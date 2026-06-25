package fr.euphyllia.skylliachat.configuration;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.IndentStyle;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlWriter;
import fr.euphyllia.skyllia.api.configuration.IConfigurationProvider;

public class ChatConfigManager implements IConfigurationProvider {

    private final CommentedFileConfig config;
    private boolean changed = false;
    private String format = "<red>[Messaging Island] %player_name%: <gray>%message%";

    public ChatConfigManager(CommentedFileConfig config) {
        this.config = config;
    }

    @Override
    public void loadConfig() {
        changed = false;
        this.format = getOrSetDefault("chat.format", format, String.class);

        if (changed) {
            TomlWriter tomlWriter = new TomlWriter();
            tomlWriter.setIndent(IndentStyle.NONE);
            tomlWriter.write(config, config.getFile(), WritingMode.REPLACE);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOrSetDefault(String path, T defaultValue, Class<T> expectedClass) {
        Object value = config.get(path);
        if (value == null) {
            config.set(path, defaultValue);
            changed = true;
            return defaultValue;
        }
        if (expectedClass.isInstance(value)) return (T) value;
        return switch (value) {
            case Integer i when expectedClass == Long.class -> (T) Long.valueOf(i);
            case Double d when expectedClass == Float.class -> (T) Float.valueOf(d.floatValue());
            case Integer i when expectedClass == Double.class -> (T) Double.valueOf(i);
            default -> throw new IllegalStateException("Cannot convert value at '" + path + "' from "
                    + value.getClass().getSimpleName() + " to " + expectedClass.getSimpleName());
        };
    }

    @Override
    public void reloadFromDisk() {
        config.load();
    }

    @Override
    public boolean canReloadFromDisk() {
        return true;
    }

    public String getFormat() {
        return format;
    }
}
