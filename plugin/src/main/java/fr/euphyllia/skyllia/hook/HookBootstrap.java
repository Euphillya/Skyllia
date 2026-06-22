package fr.euphyllia.skyllia.hook;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.hooks.PluginHook;
import fr.euphyllia.skyllia.api.hooks.SchematicHook;
import fr.euphyllia.skyllia.api.hooks.ServerHook;
import fr.euphyllia.skyllia.hook.canvas.CanvasHook;
import fr.euphyllia.skyllia.hook.fastasyncworldedit.FAWESchematicHook;
import fr.euphyllia.skyllia.hook.internal.InternalSchematicHook;
import fr.euphyllia.skyllia.hook.luminol.LuminolHook;
import fr.euphyllia.skyllia.hook.quickshop.QuickShopHook;
import fr.euphyllia.skyllia.hook.worldedit.WorldEditSchematicHook;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HookBootstrap {

    private static final Logger log = LoggerFactory.getLogger(HookBootstrap.class);

    private HookBootstrap() {
    }

    static List<ServerHook> serverHooks = List.of(
            new CanvasHook(),
            new LuminolHook()
    );

    static List<PluginHook> pluginHooks = List.of(
            new QuickShopHook()
    );

    static List<SchematicHook> schematicHooks = List.of(
        new FAWESchematicHook(),
        new WorldEditSchematicHook(),
        new InternalSchematicHook()
    );

    public static void registerAll() {
        for (ServerHook hook : serverHooks) {
            if (!hook.isAvailable()) continue;
            hook.register(Skyllia.getInstance());
            log.debug("Registered server hook: {}", hook.name());
        }

        for (SchematicHook hook : schematicHooks) {
            if (!hook.isAvailable()) continue;
            log.debug("Active schematic hook: {}", hook.name());
            break;
        }

        for (PluginHook pluginHook : pluginHooks) {
            if (!pluginHook.isAvailable()) continue;
            pluginHook.register(Skyllia.getInstance());
        }
    }

    public static void unregisterAll() {
        for (PluginHook pluginHook : pluginHooks) {
            if (pluginHook.isAvailable()) pluginHook.unregister();
        }
    }
}
