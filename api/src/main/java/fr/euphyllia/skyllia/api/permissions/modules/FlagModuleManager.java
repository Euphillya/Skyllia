package fr.euphyllia.skyllia.api.permissions.modules;

import fr.euphyllia.skyllia.api.permissions.IslandFlagRegistry;
import org.bukkit.plugin.Plugin;

import java.util.*;

public final class FlagModuleManager {

    private final Plugin core;
    private final IslandFlagRegistry registry;
    private final List<Entry> pending = new ArrayList<>();
    private final Set<FlagModule> registered = Collections.newSetFromMap(new IdentityHashMap<>());
    private boolean initialized;

    public FlagModuleManager(Plugin core, IslandFlagRegistry registry) {
        this.core = core;
        this.registry = registry;
    }

    public synchronized void addModule(Plugin owner, FlagModule module) {
        if (registered.contains(module)) return;

        if (!initialized) {
            pending.add(new Entry(owner, module));
            registered.add(module);
            return;
        }

        module.registerFlags(registry, owner);
        core.getServer().getPluginManager().registerEvents(module, core);
        registered.add(module);
    }

    public synchronized void initAndRegisterAll() {
        if (initialized) return;
        initialized = true;

        for (Entry e : pending) {
            e.module().registerFlags(registry, e.owner());
        }
        for (Entry e : pending) {
            core.getServer().getPluginManager().registerEvents(e.module(), core);
        }
        pending.clear();
    }

    private record Entry(Plugin owner, FlagModule module) {
    }
}
