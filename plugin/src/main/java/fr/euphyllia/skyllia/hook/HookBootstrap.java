package fr.euphyllia.skyllia.hook;

import fr.euphyllia.skyllia.api.hooks.PluginHook;
import fr.euphyllia.skyllia.api.hooks.SchematicHook;
import fr.euphyllia.skyllia.api.hooks.ServerHook;
import fr.euphyllia.skyllia.hook.canvas.CanvasHook;
import fr.euphyllia.skyllia.hook.fastasyncworldedit.FAWESchematicHook;
import fr.euphyllia.skyllia.hook.internal.InternalSchematicHook;
import fr.euphyllia.skyllia.hook.luminol.LuminolHook;
import fr.euphyllia.skyllia.hook.worldedit.WorldEditSchematicHook;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HookBootstrap {

    private static final Logger log = LoggerFactory.getLogger(HookBootstrap.class);

    private HookBootstrap() {
    }

    public static void registerAll(Plugin skylliaPlugin) {
        List<ServerHook> serverHooks = List.of(
                new CanvasHook(),
                new LuminolHook()
        );
        for (ServerHook hook : serverHooks) {
            if (!hook.isAvailable()) continue;
            hook.register(skylliaPlugin);
            log.info("Registered server hook: {}", hook.name());
        }

        List<SchematicHook> schematicHooks = List.of(
                new FAWESchematicHook(skylliaPlugin),
                new WorldEditSchematicHook(skylliaPlugin),
                new InternalSchematicHook(skylliaPlugin)
        );
        for (SchematicHook hook : schematicHooks) {
            if (!hook.isAvailable()) continue;
            log.info("Active schematic hook: {}", hook.name());
            break;
        }

        List<PluginHook> pluginHooks = List.of();

        for (PluginHook pluginHook : pluginHooks) {
            if (!pluginHook.isAvailable()) continue;
        }
    }
}
