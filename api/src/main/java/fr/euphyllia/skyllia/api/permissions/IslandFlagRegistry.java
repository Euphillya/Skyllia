package fr.euphyllia.skyllia.api.permissions;

import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class IslandFlagRegistry {

    private final PermissionIndexStore indexStore;

    private final Map<NamespacedKey, FlagId> ids = new HashMap<>();
    private final List<FlagNode> byIndex = new ArrayList<>();

    private int version = 0;
    private int maxIndex = -1;

    public IslandFlagRegistry(PermissionIndexStore indexStore) {
        this.indexStore = indexStore;
    }

    private static String toNodeKey(NamespacedKey key) {
        return "flag:" + key.getNamespace() + ":" + key.getKey();
    }

    public synchronized int version() {
        return version;
    }

    public synchronized int size() {
        return Math.max(0, maxIndex + 1);
    }

    public synchronized FlagId register(FlagNode node) {
        FlagId existing = ids.get(node.node());
        if (existing != null) return existing;

        int idx = indexStore.getOrAllocate(toNodeKey(node.node()));
        FlagId id = new FlagId(idx);

        ids.put(node.node(), id);
        putByIndex(idx, node);

        version++;
        return id;
    }

    public synchronized FlagId getIfPresent(NamespacedKey node) {
        return ids.get(node);
    }

    public synchronized FlagId idOrRegister(FlagNode node) {
        FlagId existing = ids.get(node.node());
        if (existing != null) return existing;
        return register(node);
    }

    public synchronized FlagId id(NamespacedKey node) {
        FlagId id = ids.get(node);
        if (id == null) throw new IllegalArgumentException("Unknown flag node: " + node);
        return id;
    }

    public synchronized FlagNode node(FlagId id) {
        int idx = id.index();
        if (idx < 0 || idx >= byIndex.size()) {
            throw new IndexOutOfBoundsException("Invalid FlagId: " + idx);
        }
        FlagNode node = byIndex.get(idx);
        if (node == null) {
            throw new IllegalStateException("No FlagNode registered for index: " + idx);
        }
        return node;
    }

    public synchronized List<NamespacedKey> keys() {
        return new ArrayList<>(ids.keySet());
    }

    public synchronized Map<NamespacedKey, FlagId> entries() {
        return Map.copyOf(ids);
    }

    private void putByIndex(int idx, FlagNode node) {
        if (idx > maxIndex) maxIndex = idx;

        ensureByIndexCapacity(idx + 1);

        FlagNode previous = byIndex.get(idx);
        if (previous != null && !previous.node().equals(node.node())) {
            throw new IllegalStateException(
                    "Flag index collision: idx=" + idx + " already mapped to " + previous.node()
                            + ", attempted to map " + node.node()
            );
        }
        byIndex.set(idx, node);
    }

    private void ensureByIndexCapacity(int size) {
        while (byIndex.size() < size) byIndex.add(null);
    }
}
