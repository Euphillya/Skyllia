package fr.euphyllia.skyllia.hook;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.hooks.*;
import fr.euphyllia.skyllia.hook.canvas.CanvasHook;
import fr.euphyllia.skyllia.hook.cmi.CMISpawnHook;
import fr.euphyllia.skyllia.hook.essentialsx.EssentialsSpawnHook;
import fr.euphyllia.skyllia.hook.fastasyncworldedit.FAWESchematicHook;
import fr.euphyllia.skyllia.hook.internal.InternalSchematicHook;
import fr.euphyllia.skyllia.hook.luckperms.LuckPermsHook;
import fr.euphyllia.skyllia.hook.luminol.LuminolHook;
import fr.euphyllia.skyllia.hook.quickshop.QuickShopHook;
import fr.euphyllia.skyllia.hook.worldedit.WorldEditSchematicHook;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HookBootstrap {

    static final List<ServerHook> serverHooks = List.of(
            new CanvasHook(),
            new LuminolHook()
    );

    static final List<PluginHook> pluginHooks = List.of(
            new LuckPermsHook(),
            new CMISpawnHook(),
            new EssentialsSpawnHook(),
            new QuickShopHook()
    );

    static final FAWESchematicHook faweHook = new FAWESchematicHook();
    static final WorldEditSchematicHook worldEditHook = new WorldEditSchematicHook();
    static final InternalSchematicHook internalHook = new InternalSchematicHook();
    static final List<SchematicHook> schematicHooks = List.of(
            faweHook,
            worldEditHook,
            internalHook
    );
    private static final Logger log = LoggerFactory.getLogger(HookBootstrap.class);

    private static @Nullable SpawnHook activeSpawnHook = null;
    private static @Nullable PermissionHook activePermissionHook = null;

    private HookBootstrap() {
    }

    public static void registerAll() {
        for (ServerHook hook : serverHooks) {
            if (!hook.isAvailable()) continue;
            hook.register(Skyllia.getInstance());
            log.debug("Registered server hook: {}", hook.name());
        }

        for (SchematicHook schematicHook : schematicHooks) {
            if (!schematicHook.isAvailable()) continue;
            schematicHook.register(Skyllia.getInstance());
            log.debug("Active schematic hook: {}", schematicHook.name());
            break;
        }

        boolean spawnHookFound = false;
        for (PluginHook hook : pluginHooks) {
            if (!hook.isAvailable()) continue;
            if (hook instanceof SpawnHook spawnHook) {
                if (spawnHookFound) continue;
                spawnHook.register(Skyllia.getInstance());
                activeSpawnHook = spawnHook;
                spawnHookFound = true;
                log.debug("Active spawn hook: {}", spawnHook.name());
                continue;
            }

            if (hook instanceof PermissionHook permissionHook) {
                if (activePermissionHook != null) continue;
                permissionHook.register(Skyllia.getInstance());
                activePermissionHook = permissionHook;
                log.debug("Active permission hook: {}", permissionHook.name());
                continue;
            }

            hook.register(Skyllia.getInstance());
            log.error("Registered plugin hook: {}", hook.name());
        }
    }

    public static void unregisterAll() {
        for (PluginHook pluginHook : pluginHooks) {
            if (pluginHook.isAvailable()) pluginHook.unregister();
        }

        for (SchematicHook schematicHook : schematicHooks) {
            if (schematicHook.isAvailable()) schematicHook.unregister();
        }
    }

    public static @Nullable SpawnHook getSpawnHook() {
        return activeSpawnHook;
    }

    public static @Nullable PermissionHook getPermissionHook() {
        return activePermissionHook;
    }
}
