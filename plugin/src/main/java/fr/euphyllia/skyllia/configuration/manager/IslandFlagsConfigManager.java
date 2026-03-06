package fr.euphyllia.skyllia.configuration.manager;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.IndentStyle;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlWriter;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.configuration.IConfigurationProvider;
import fr.euphyllia.skyllia.api.permissions.*;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class IslandFlagsConfigManager implements IConfigurationProvider {

    private static final Logger log = LoggerFactory.getLogger(IslandFlagsConfigManager.class);

    private final CommentedFileConfig config;

    private final Map<String, Map<FlagId, Boolean>> compiledDefaults = new HashMap<>();

    private boolean changed = false;
    private int configVersion;
    private boolean verbose;

    public IslandFlagsConfigManager(CommentedFileConfig config) {
        this.config = config;
    }

    private static void applyInto(IslandFlags target,
                                  IslandFlagRegistry registry,
                                  @Nullable Map<FlagId, Boolean> source) {
        if (source == null) return;
        for (Map.Entry<FlagId, Boolean> e : source.entrySet()) {
            target.set(registry, e.getKey(), e.getValue());
        }
    }

    private static List<String> literalPath(String key) {
        return Collections.singletonList(key);
    }

    private static @Nullable Object getLiteral(CommentedConfig cfg, String literalKey) {
        return cfg.get(literalPath(literalKey));
    }

    private static @Nullable NamespacedKey parseNamespacedKey(String s) {
        if (s == null) return null;
        int idx = s.indexOf(':');
        if (idx <= 0 || idx == s.length() - 1) return null;
        try {
            return new NamespacedKey(s.substring(0, idx), s.substring(idx + 1));
        } catch (Exception ignored) {
            return null;
        }
    }


    private static @Nullable Boolean coerceBoolean(Object o) {
        if (o instanceof Boolean b) return b;
        if (o instanceof String s) {
            String v = s.trim().toLowerCase(Locale.ROOT);
            if (v.equals("true") || v.equals("on") || v.equals("yes")) return true;
            if (v.equals("false") || v.equals("off") || v.equals("no")) return false;
        }
        return null;
    }

    @Override
    public void loadConfig() {
        changed = false;
        this.configVersion = getOrSetDefault("config-version", 1, Integer.class);
        this.verbose = getOrSetDefault("verbose", false, Boolean.class);

        IslandFlagRegistry registry = SkylliaAPI.getFlagRegistry();

        ensureAllDefaultsExist(registry);

        compiledDefaults.clear();
        readDefaultsFlat(registry, null, config.get("defaults"));

        Object islandObj = config.get("island");
        if (islandObj instanceof CommentedConfig islandRoot) {
            List<String> islandTypes = new ArrayList<>(islandRoot.valueMap().keySet());
            islandTypes.sort(String::compareTo);
            for (String islandType : islandTypes) {
                Object islandNodeObj = islandRoot.get(islandType);
                if (!(islandNodeObj instanceof CommentedConfig islandNode)) continue;
                readDefaultsFlat(registry, islandType, islandNode.get("defaults"));
            }
        }

        if (changed) {
            TomlWriter tomlWriter = new TomlWriter();
            tomlWriter.setIndent(IndentStyle.NONE);
            tomlWriter.write(config, config.getFile(), WritingMode.REPLACE);
        }
    }

    @Override
    public void reloadFromDisk() {
        config.load();
    }

    @Override
    public boolean canReloadFromDisk() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOrSetDefault(String path, T defaultValue, Class<T> expected) {
        Object value = config.get(path);
        if (value == null) {
            config.set(path, defaultValue);
            changed = true;
            return defaultValue;
        }
        if (expected.isInstance(value)) return (T) value;
        if (expected == Long.class && value instanceof Integer i) return (T) Long.valueOf(i);
        if (expected == Float.class && value instanceof Double d) return (T) Float.valueOf(d.floatValue());
        throw new IllegalStateException("Cannot convert value at path '" + path + "' from "
                + value.getClass().getSimpleName() + " to " + expected.getSimpleName());
    }


    public byte[] buildDefaultFlagBlob(@Nullable String islandType) {
        IslandFlagRegistry registry = SkylliaAPI.getFlagRegistry();
        IslandFlags flags = new IslandFlags(registry);

        applyInto(flags, registry, compiledDefaults.get(null));
        if (islandType != null) {
            applyInto(flags, registry, compiledDefaults.get(islandType));
        }

        flags.ensureUpToDate(registry);
        return PermissionSetCodec.encodeLongs(flags.snapshotWords());
    }

    public int getConfigVersion() {
        return configVersion;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void compileNow() {
        loadConfig();
    }

    private void ensureAllDefaultsExist(IslandFlagRegistry registry) {
        CommentedConfig defaultsRoot = getOrCreateSub(config);
        ensureFlatBlockContainsAll(registry, defaultsRoot);

        Object islandObj = config.get("island");
        if (!(islandObj instanceof CommentedConfig islandRoot)) return;

        for (String islandType : new TreeSet<>(islandRoot.valueMap().keySet())) {
            Object islandNodeObj = islandRoot.get(islandType);
            if (!(islandNodeObj instanceof CommentedConfig islandNode)) continue;
            CommentedConfig islandDefaultsRoot = getOrCreateSub(islandNode);
            ensureFlatBlockContainsAll(registry, islandDefaultsRoot);
        }
    }

    private void ensureFlatBlockContainsAll(IslandFlagRegistry registry, CommentedConfig block) {
        List<NamespacedKey> keys = new ArrayList<>(registry.keys());
        keys.sort(Comparator.comparing(NamespacedKey::getNamespace).thenComparing(NamespacedKey::getKey));

        for (NamespacedKey k : keys) {
            String flatKey = k.getNamespace() + ":" + k.getKey();
            if (getLiteral(block, flatKey) != null) continue;

            FlagNode node = registry.node(registry.id(k));
            setLiteral(block, flatKey, node.defaultValue());
            changed = true;
        }
    }

    private void readDefaultsFlat(IslandFlagRegistry registry,
                                  @Nullable String islandType,
                                  @Nullable Object defaultsObj) {
        if (!(defaultsObj instanceof CommentedConfig defaultsBlock)) return;

        Map<FlagId, Boolean> flagMap = compiledDefaults
                .computeIfAbsent(islandType, k -> new HashMap<>());

        List<Map.Entry<String, Object>> entries = new ArrayList<>(defaultsBlock.valueMap().entrySet());
        entries.sort(Map.Entry.comparingByKey());

        for (Map.Entry<String, Object> entry : entries) {
            String permString = entry.getKey();
            Object valueObj = entry.getValue();

            if (valueObj instanceof CommentedConfig) continue; // nested — skip

            Boolean value = coerceBoolean(valueObj);
            if (value == null) continue;

            NamespacedKey key = parseNamespacedKey(permString);
            if (key == null) continue;

            FlagId id = registry.getIfPresent(key);
            if (id == null) {
                if (verbose) {
                    log.info("Unknown flag '{}' in flags.toml (islandType='{}')", key, islandType);
                }
                continue;
            }
            flagMap.put(id, value);
        }
    }

    private CommentedConfig getOrCreateSub(CommentedConfig parent) {
        Object obj = parent.get("defaults");
        if (obj instanceof CommentedConfig cc) return cc;
        CommentedConfig created = config.createSubConfig();
        parent.set("defaults", created);
        changed = true;
        return created;
    }

    private void setLiteral(CommentedConfig cfg, String literalKey, Object value) {
        cfg.set(literalPath(literalKey), value);
    }
}
