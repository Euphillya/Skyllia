package fr.euphyllia.skyllia.api.permissions;

import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PermissionRegistry {
    private final Map<NamespacedKey, PermissionId> ids = new HashMap<>();
    private final List<PermissionNode> nodes = new ArrayList<>();

    private int version = 0;

    public synchronized int version() {
        return version;
    }

    public synchronized int size() {
        return nodes.size();
    }

    public synchronized PermissionId register(PermissionNode node) {
        PermissionId existing = ids.get(node.node());
        if (existing != null) return existing;

        PermissionId id = new PermissionId(nodes.size());
        nodes.add(node);
        ids.put(node.node(), id);
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
        if (idx < 0 || idx >= nodes.size()) throw new IndexOutOfBoundsException("Invalid PermissionId: " + idx);
        return nodes.get(idx);
    }

}
