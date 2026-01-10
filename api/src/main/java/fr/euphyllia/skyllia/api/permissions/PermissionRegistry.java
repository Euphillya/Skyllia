package fr.euphyllia.skyllia.api.permissions;

import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PermissionRegistry {

    private final PermissionIndexStore indexStore;

    private final Map<NamespacedKey, PermissionId> ids = new HashMap<>();
    private final List<PermissionNode> byIndex = new ArrayList<>();

    private int version = 0;
    private int maxIndex = -1;

    public PermissionRegistry(PermissionIndexStore indexStore) {
        this.indexStore = indexStore;
    }

    public synchronized int version() {
        return version;
    }

    public synchronized int size() {
        return Math.max(0, maxIndex + 1);
    }

    public synchronized PermissionId register(PermissionNode node) {
        PermissionId existing = ids.get(node.node());
        if (existing != null) return existing;

        int idx = indexStore.getOrAllocate(toNodeKey(node.node()));
        PermissionId id = new PermissionId(idx);

        ids.put(node.node(), id);
        putByIndex(idx, node);

        version++;
        return id;
    }

    public synchronized PermissionId getIfPresent(NamespacedKey node) {
        return ids.get(node);
    }

    public synchronized PermissionId idOrRegister(PermissionNode node) {
        PermissionId existing = ids.get(node.node());
        if (existing != null) return existing;
        return register(node);
    }

    public synchronized PermissionId id(NamespacedKey node) {
        PermissionId id = ids.get(node);
        if (id == null) throw new IllegalArgumentException("Unknown permission node: " + node);
        return id;
    }

    public synchronized PermissionNode node(PermissionId id) {
        int idx = id.index();
        if (idx < 0 || idx >= byIndex.size()) {
            throw new IndexOutOfBoundsException("Invalid PermissionId: " + idx);
        }
        PermissionNode node = byIndex.get(idx);
        if (node == null) {
            throw new IllegalStateException("No PermissionNode registered for index: " + idx);
        }
        return node;
    }

    private void putByIndex(int idx, PermissionNode node) {
        if (idx > maxIndex) maxIndex = idx;

        ensureByIndexCapacity(idx + 1);

        PermissionNode previous = byIndex.get(idx);
        if (previous != null && !previous.node().equals(node.node())) {
            throw new IllegalStateException(
                    "Permission index collision: idx=" + idx + " already mapped to " + previous.node()
                            + ", attempted to map " + node.node()
            );
        }
        byIndex.set(idx, node);
    }

    private void ensureByIndexCapacity(int size) {
        while (byIndex.size() < size) byIndex.add(null);
    }

    private static String toNodeKey(NamespacedKey key) {
        return key.getNamespace() + ":" + key.getKey();
    }

    public synchronized List<NamespacedKey> keys() {
        return new ArrayList<>(ids.keySet());
    }

}
