package fr.euphyllia.skylliaacidrain.configuration;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.IndentStyle;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlWriter;
import fr.euphyllia.skyllia.api.configuration.IConfigurationProvider;
import fr.euphyllia.skylliaacidrain.listener.AcidListener;

public class AcidConfigManager implements IConfigurationProvider {

    private final AcidListener acidListener;
    private final CommentedFileConfig config;
    private boolean changed = false;

    private double damage;
    private long damageIntervalTick;
    private boolean particles;
    private boolean sound;
    private String soundName;

    private boolean onlyPlayer;

    private boolean poisonEnabled;
    private int poisonDuration;
    private int poisonAmplifier;
    private boolean slownessEnabled;
    private int slownessDuration;
    private int slownessAmplifier;
    private boolean nauseaEnabled;
    private int nauseaDuration;
    private int nauseaAmplifier;

    public AcidConfigManager(CommentedFileConfig config, AcidListener listener) {
        this.config = config;
        this.acidListener = listener;
    }

    @Override
    public void loadConfig() {
        changed = false;

        this.damage = getOrSetDefault("acid.damage", 2.0, Double.class);
        this.damageIntervalTick = getOrSetDefault("acid.damage-interval-tick", 20L, Long.class); // 20 tick = 1 second

        this.particles = getOrSetDefault("acid.particles", true, Boolean.class);
        this.sound = getOrSetDefault("acid.sound.enabled", true, Boolean.class);
        this.soundName = getOrSetDefault("acid.sound.name", "BLOCK_LAVA_AMBIENT", String.class);

        this.onlyPlayer = getOrSetDefault("acid.only-player", false, Boolean.class);

        this.poisonEnabled = getOrSetDefault("acid.effects.poison.enabled", true, Boolean.class);
        this.poisonDuration = getOrSetDefault("acid.effects.poison.duration", 60, Integer.class);
        this.poisonAmplifier = getOrSetDefault("acid.effects.poison.amplifier", 1, Integer.class);

        this.slownessEnabled = getOrSetDefault("acid.effects.slowness.enabled", true, Boolean.class);
        this.slownessDuration = getOrSetDefault("acid.effects.slowness.duration", 40, Integer.class);
        this.slownessAmplifier = getOrSetDefault("acid.effects.slowness.amplifier", 0, Integer.class);

        this.nauseaEnabled = getOrSetDefault("acid.effects.nausea.enabled", true, Boolean.class);
        this.nauseaDuration = getOrSetDefault("acid.effects.nausea.duration", 80, Integer.class);
        this.nauseaAmplifier = getOrSetDefault("acid.effects.nausea.amplifier", 0, Integer.class);

        this.acidListener.restartAllTasks();

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

    public double getDamage() {
        return damage;
    }

    public long getDamageIntervalTick() {
        return damageIntervalTick;
    }

    public boolean isParticles() {
        return particles;
    }

    public boolean isSound() {
        return sound;
    }

    public String getSoundName() {
        return soundName;
    }

    public boolean isOnlyPlayer() {
        return onlyPlayer;
    }

    public boolean isPoisonEnabled() {
        return poisonEnabled;
    }

    public int getPoisonDuration() {
        return poisonDuration;
    }

    public int getPoisonAmplifier() {
        return poisonAmplifier;
    }

    public boolean isSlownessEnabled() {
        return slownessEnabled;
    }

    public int getSlownessDuration() {
        return slownessDuration;
    }

    public int getSlownessAmplifier() {
        return slownessAmplifier;
    }

    public boolean isNauseaEnabled() {
        return nauseaEnabled;
    }

    public int getNauseaDuration() {
        return nauseaDuration;
    }

    public int getNauseaAmplifier() {
        return nauseaAmplifier;
    }

}
