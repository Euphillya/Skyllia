package fr.euphyllia.skyllia.api.permissions.modules;

import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import org.bukkit.plugin.Plugin;

import java.util.*;

public final class PermissionModuleManager {
    private final Plugin core;
    private final PermissionRegistry registry;
    private final List<Entry> pending = new ArrayList<>();
    private boolean initialized;
    private final Set<PermissionModule> registered = Collections.newSetFromMap(new IdentityHashMap<>());

    private record Entry(Plugin owner, PermissionModule module) {
    }

    public PermissionModuleManager(Plugin core, PermissionRegistry registry) {
        this.core = core;
        this.registry = registry;
    }

    public synchronized void addModule(Plugin owner, PermissionModule module) {
        if (registered.contains(module)) return;

        if (!initialized) {
            pending.add(new Entry(owner, module));
            registered.add(module);
            return;
        }

        module.registerPermissions(registry, owner);
        core.getServer().getPluginManager().registerEvents(module, core);
        registered.add(module);
    }

    public synchronized void initAndRegisterAll() {
        if (initialized) return;
        initialized = true;

        for (Entry e : pending) {
            e.module().registerPermissions(registry, e.owner());
        }

        for (Entry e : pending) {
            core.getServer().getPluginManager().registerEvents(e.module(), core);
        }

        pending.clear();
    }
}
