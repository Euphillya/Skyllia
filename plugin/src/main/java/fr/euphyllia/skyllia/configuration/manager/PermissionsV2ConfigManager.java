package fr.euphyllia.skyllia.configuration.manager;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.IndentStyle;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlWriter;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.permissions.PermissionSet;
import fr.euphyllia.skyllia.api.permissions.PermissionSetCodec;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.managers.ConfigManager;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PermissionsV2ConfigManager implements ConfigManager {

    private static final Logger log = LoggerFactory.getLogger(PermissionsV2ConfigManager.class);

    private final CommentedFileConfig config;

    private final Map<String, EnumMap<RoleType, Map<PermissionId, Boolean>>> compiledDefaults = new HashMap<>();

    private boolean changed = false;
    private int configVersion;
    private boolean verbose;

    public PermissionsV2ConfigManager(CommentedFileConfig config) {
        this.config = config;
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
        String ns = s.substring(0, idx);
        String key = s.substring(idx + 1);
        try {
            return new NamespacedKey(ns, key);
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

    private static void applyInto(EnumMap<RoleType, PermissionSet> target,
                                  PermissionRegistry registry,
                                  @Nullable EnumMap<RoleType, Map<PermissionId, Boolean>> source) {
        if (source == null) return;

        for (Map.Entry<RoleType, Map<PermissionId, Boolean>> roleEntry : source.entrySet()) {
            RoleType role = roleEntry.getKey();
            PermissionSet set = target.computeIfAbsent(role, r -> new PermissionSet(registry.size()));
            set.ensureCapacity(registry.size());

            for (Map.Entry<PermissionId, Boolean> permEntry : roleEntry.getValue().entrySet()) {
                set.set(permEntry.getKey(), permEntry.getValue());
            }
        }
    }

    private void setLiteral(CommentedConfig cfg, String literalKey, Object value) {
        cfg.set(literalPath(literalKey), value);
    }

    private boolean defaultValueFor(RoleType role, NamespacedKey key) {
        // Safe default everywhere.
        return false;
    }

    private CommentedConfig getOrCreateSub(CommentedConfig parent, String key) {
        Object obj = parent.get(key);
        if (obj instanceof CommentedConfig cc) return cc;

        CommentedConfig created = config.createSubConfig();
        parent.set(key, created);
        changed = true;
        return created;
    }

    private List<RoleType> stableRoles() {
        List<RoleType> roles = new ArrayList<>(Arrays.asList(RoleType.values()));
        roles.sort(Comparator.comparingInt(RoleType::getValue).reversed().thenComparing(Enum::name));
        return roles;
    }

    private List<NamespacedKey> stableKeys(PermissionRegistry registry) {
        List<NamespacedKey> keys = registry.keys();
        keys.sort(Comparator.comparing(NamespacedKey::getNamespace).thenComparing(NamespacedKey::getKey));
        return keys;
    }

    private void migrateDefaultsRootToFlat(@Nullable CommentedConfig defaultsRoot) {
        if (defaultsRoot == null) return;

        for (String roleKey : new ArrayList<>(defaultsRoot.valueMap().keySet())) {
            Object roleNodeObj = defaultsRoot.get(roleKey);
            if (!(roleNodeObj instanceof CommentedConfig roleNode)) continue;

            boolean hasNested = false;
            for (Object v : roleNode.valueMap().values()) {
                if (v instanceof CommentedConfig) {
                    hasNested = true;
                    break;
                }
            }
            if (!hasNested) continue;

            Map<String, Boolean> flat = new TreeMap<>();
            flattenRoleNode(roleNode, "", flat);

            for (String k : new ArrayList<>(roleNode.valueMap().keySet())) {
                roleNode.remove(k);
            }
            for (Map.Entry<String, Boolean> e : flat.entrySet()) {
                setLiteral(roleNode, e.getKey(), e.getValue());
            }
            changed = true;
        }
    }


    private void flattenRoleNode(CommentedConfig node, String prefix, Map<String, Boolean> out) {
        // node contains entries like:
        // "skyllia:block" -> CommentedConfig { break=false, use -> CommentedConfig { bucket=false } }
        for (Map.Entry<String, Object> entry : node.valueMap().entrySet()) {
            String k = entry.getKey();
            Object v = entry.getValue();

            String nextPrefix = prefix.isEmpty() ? k : (prefix + "." + k);

            if (v instanceof CommentedConfig cc) {
                flattenRoleNode(cc, nextPrefix, out);
            } else {
                Boolean b = coerceBoolean(v);
                if (b != null) {
                    out.put(nextPrefix, b);
                }
            }
        }
    }

    private void ensureAllDefaultsExist(PermissionRegistry registry) {
        CommentedConfig defaultsRoot = getOrCreateSub(config, "defaults");
        migrateDefaultsRootToFlat(defaultsRoot);
        ensureRoleBlocksContainAllFlat(registry, defaultsRoot);

        Object islandObj = config.get("island");
        if (!(islandObj instanceof CommentedConfig islandRoot)) return;

        for (String islandType : new TreeSet<>(islandRoot.valueMap().keySet())) {
            Object islandNodeObj = islandRoot.get(islandType);
            if (!(islandNodeObj instanceof CommentedConfig islandNode)) continue;

            CommentedConfig islandDefaultsRoot = getOrCreateSub(islandNode, "defaults");
            migrateDefaultsRootToFlat(islandDefaultsRoot);
            ensureRoleBlocksContainAllFlat(registry, islandDefaultsRoot);
        }
    }

    private void ensureRoleBlocksContainAllFlat(PermissionRegistry registry, CommentedConfig defaultsRoot) {
        List<RoleType> roles = stableRoles();
        List<NamespacedKey> keys = stableKeys(registry);

        for (RoleType role : roles) {
            CommentedConfig roleNode = getOrCreateSub(defaultsRoot, role.name());

            for (NamespacedKey k : keys) {
                String flatKey = k.getNamespace() + ":" + k.getKey();
                if (getLiteral(roleNode, flatKey) != null) continue;

                setLiteral(roleNode, flatKey, defaultValueFor(role, k));
                changed = true;
            }
        }
    }

    private void readDefaultsFlat(PermissionRegistry registry, @Nullable String islandType, @Nullable CommentedConfig defaultsRoot) {
        if (defaultsRoot == null) return;

        EnumMap<RoleType, Map<PermissionId, Boolean>> roleMap =
                compiledDefaults.computeIfAbsent(islandType, k -> new EnumMap<>(RoleType.class));

        List<String> roleKeys = new ArrayList<>(defaultsRoot.valueMap().keySet());
        roleKeys.sort(String::compareTo);

        for (String roleKey : roleKeys) {
            RoleType role;
            try {
                role = RoleType.valueOf(roleKey);
            } catch (Exception ignored) {
                continue;
            }

            Object roleNodeObj = defaultsRoot.get(roleKey);
            if (!(roleNodeObj instanceof CommentedConfig roleNode)) continue;

            Map<PermissionId, Boolean> perms = roleMap.computeIfAbsent(role, r -> new HashMap<>());

            List<Map.Entry<String, Object>> entries = new ArrayList<>(roleNode.valueMap().entrySet());
            entries.sort(Map.Entry.comparingByKey());

            for (Map.Entry<String, Object> entry : entries) {
                String permString = entry.getKey();
                Object valueObj = entry.getValue();

                if (valueObj instanceof CommentedConfig) continue;

                Boolean value = coerceBoolean(valueObj);
                if (value == null) continue;

                NamespacedKey key = parseNamespacedKey(permString);
                if (key == null) continue;

                PermissionId pid = registry.getIfPresent(key);
                if (pid == null) {
                    if (verbose) {
                        log.info("Unknown permission '{}' in defaults (islandType='{}', role='{}')",
                                key, islandType, role);
                    }
                    continue;
                }

                perms.put(pid, value);
            }
        }
    }

    @Override
    public void loadConfig() {
        changed = false;

        this.configVersion = getOrSetDefault("config-version", 1, Integer.class);
        this.verbose = getOrSetDefault("verbose", false, Boolean.class);

        PermissionRegistry registry = SkylliaAPI.getPermissionRegistry();

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

        throw new IllegalStateException("Cannot convert value at path '" + path + "' from " +
                value.getClass().getSimpleName() + " to " + expected.getSimpleName());
    }

    public EnumMap<RoleType, byte[]> buildDefaultRoleBlobs(@Nullable String islandType) {
        PermissionRegistry registry = SkylliaAPI.getPermissionRegistry();
        EnumMap<RoleType, PermissionSet> sets = new EnumMap<>(RoleType.class);

        // global defaults
        applyInto(sets, registry, compiledDefaults.get(null));

        // per-island-type overrides
        if (islandType != null) {
            applyInto(sets, registry, compiledDefaults.get(islandType));
        }

        EnumMap<RoleType, byte[]> out = new EnumMap<>(RoleType.class);
        for (Map.Entry<RoleType, PermissionSet> e : sets.entrySet()) {
            PermissionSet set = e.getValue();
            set.ensureCapacity(registry.size());

            long[] words = set.snapshotWords();
            byte[] blob = PermissionSetCodec.encodeLongs(words);
            out.put(e.getKey(), blob);
        }
        return out;
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
}
