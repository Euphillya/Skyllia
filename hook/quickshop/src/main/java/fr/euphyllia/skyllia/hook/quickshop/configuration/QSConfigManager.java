package fr.euphyllia.skyllia.hook.quickshop.configuration;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.IndentStyle;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlWriter;
import fr.euphyllia.skyllia.api.configuration.IConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QSConfigManager implements IConfigurationProvider {

    private static final Logger log = LoggerFactory.getLogger(QSConfigManager.class);

    private final CommentedFileConfig config;
    private boolean changed = false;

    /**
     * If true, only the island owner can create a shop on the island.
     * Ignores the island permission system entirely when enabled.
     */
    private boolean onlyOwnerCanCreateShop;

    /**
     * If true, all shops owned by a member are deleted when that member
     * leaves or is kicked from the island.
     */
    private boolean deleteShopOnMemberLeave;

    public QSConfigManager(CommentedFileConfig config) {
        this.config = config;
    }

    @Override
    public void loadConfig() {
        changed = false;

        this.onlyOwnerCanCreateShop = getOrSetDefault("only-owner-can-create-shop", false, Boolean.class);
        this.deleteShopOnMemberLeave = getOrSetDefault("delete-shop-on-member-leave", true, Boolean.class);

        if (changed) {
            TomlWriter tomlWriter = new TomlWriter();
            tomlWriter.setIndent(IndentStyle.NONE);
            tomlWriter.write(config, config.getFile(), WritingMode.REPLACE);
        }

        log.info("(Re)Loaded config QuickShop hook");
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
            default -> throw new IllegalStateException(
                    "Cannot convert value at '" + path + "' from "
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

    public boolean isOnlyOwnerCanCreateShop() {
        return onlyOwnerCanCreateShop;
    }

    public boolean isDeleteShopOnMemberLeave() {
        return deleteShopOnMemberLeave;
    }
}
