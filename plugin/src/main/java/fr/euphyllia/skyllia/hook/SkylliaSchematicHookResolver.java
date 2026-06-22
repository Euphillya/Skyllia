package fr.euphyllia.skyllia.hook;

import fr.euphyllia.skyllia.api.hooks.SchematicHook;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicPlugin;
import fr.euphyllia.skyllia.hook.fastasyncworldedit.FAWESchematicHook;
import fr.euphyllia.skyllia.hook.internal.InternalSchematicHook;
import fr.euphyllia.skyllia.hook.worldedit.WorldEditSchematicHook;
import org.bukkit.plugin.java.JavaPlugin;

public class SkylliaSchematicHookResolver {

    private final SchematicHook fawe;
    private final SchematicHook we;
    private final InternalSchematicHook internal;

    public SkylliaSchematicHookResolver(JavaPlugin plugin) {
        this.internal = new InternalSchematicHook();
        this.fawe = tryLoad(FAWESchematicHook::new);
        this.we = tryLoad(WorldEditSchematicHook::new);
    }

    private SchematicHook tryLoad(java.util.function.Supplier<SchematicHook> supplier) {
        try {
            SchematicHook hook = supplier.get();
            return hook.isAvailable() ? hook : null;
        } catch (NoClassDefFoundError | Exception e) {
            return null;
        }
    }

    public SchematicHook resolve(SchematicPlugin requested) {
        return switch (requested) {
            case WORLD_EDIT -> fawe != null ? fawe : we != null ? we : internal;
            case INTERNAL -> internal;
            case UNKNOWN -> fawe != null ? fawe : we != null ? we : internal;
        };
    }
}
