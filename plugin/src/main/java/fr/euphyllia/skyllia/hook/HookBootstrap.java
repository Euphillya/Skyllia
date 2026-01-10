package fr.euphyllia.skyllia.hook;

import fr.euphyllia.skyllia.api.hooks.ServerHook;
import fr.euphyllia.skyllia.hook.canvas.CanvasHook;
import fr.euphyllia.skyllia.hook.luminol.LuminolHook;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HookBootstrap {

    private static final Logger log = LoggerFactory.getLogger(HookBootstrap.class);

    private HookBootstrap() {
    }

    public static void registerAll(Plugin skylliaPlugin) {
        List<ServerHook> hooks = List.of(
                new CanvasHook(),
                new LuminolHook()
        );

        for (ServerHook hook : hooks) {
            if (!hook.isAvailable()) continue;

            hook.register(skylliaPlugin);
            log.debug("Registered hook: {}", hook.name());
        }
    }

}
