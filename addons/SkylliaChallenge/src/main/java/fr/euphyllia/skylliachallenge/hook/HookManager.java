package fr.euphyllia.skylliachallenge.hook;

import fr.euphyllia.skylliachallenge.api.CustomItemSupport;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HookManager {

    private static final Map<String, CustomItemSupport> HOOKS = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(HookManager.class);

    public static void init() {
        HOOKS.clear();
        if (Bukkit.getPluginManager().isPluginEnabled("Nexo")) {
            try {
                register(new NexoSupport());
                log.info("Nexo hook registered.");
            } catch (Throwable t) {
                log.warn("Unable to register Nexo hook: {}", t.getMessage());
            }
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Oraxen")) {
            try {
                register(new OraxenSupport());
                log.info("Oraxen hook registered.");
            } catch (Throwable t) {
                log.warn("Unable to register Oraxen hook: {}", t.getMessage());
            }
        }
    }

    public static void register(CustomItemSupport support) {
        if (support == null || support.getNamespace() == null) return;
        HOOKS.put(support.getNamespace().toLowerCase(Locale.ROOT), support);
    }

    public static CustomItemSupport get(String namespace) {
        if (namespace == null) return null;
        return HOOKS.get(namespace.toLowerCase(Locale.ROOT));
    }

    public static boolean has(String namespace) {
        return get(namespace) != null;
    }

    public static boolean isCustomItemRef(String raw) {
        CustomItemRef ref = parse(raw);
        return ref != null;
    }


    public static CustomItemRef parse(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        int colon = raw.indexOf(':');
        if (colon <= 0 || colon >= raw.length() - 1) return null;
        String namespace = raw.substring(0, colon).toLowerCase(Locale.ROOT);
        String id = raw.substring(colon + 1);
        if ("minecraft".equals(namespace)) return null;
        CustomItemSupport support = get(namespace);
        if (support == null) return null;
        return new CustomItemRef(namespace, id, support);
    }

    public static ItemStack itemStackFromRef(String raw) {
        CustomItemRef ref = parse(raw);
        if (ref == null) return null;
        return ref.getItemStack();
    }

    public static boolean matches(ItemStack item, String namespace, String id) {
        CustomItemSupport s = get(namespace);
        if (s == null) return false;
        return s.matches(item, id);
    }

    public static boolean matchesBlock(Block block, String namespace, String id) {
        CustomItemSupport s = get(namespace);
        if (s == null) return false;
        return s.matchesBlock(block, id);
    }

    public record CustomItemRef(String namespace, String id, CustomItemSupport support) {

        public ItemStack getItemStack() {
            return support.getItemFromId(id);
        }

        public boolean matches(ItemStack item) {
            return support.matches(item, id);
        }

        public boolean matchesBlock(Block block) {
            return support.matchesBlock(block, id);
        }

        public String fullId() {
            return namespace + ":" + id;
        }
    }
}
