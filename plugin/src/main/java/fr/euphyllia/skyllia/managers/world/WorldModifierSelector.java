package fr.euphyllia.skyllia.managers.world;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicPlugin;
import fr.euphyllia.skyllia.api.world.WorldModifier;
import fr.euphyllia.skyllia.hook.InternalWorldModifier;
import fr.euphyllia.skyllia.hook.fastasyncworldedit.FastAsyncWorldEditUtils;
import fr.euphyllia.skyllia.hook.worldedit.WorldEditUtils;

public record WorldModifierSelector(Skyllia plugin, boolean faweEnabled, boolean weEnabled) {

    private static WorldModifier fawe;
    private static WorldModifier we;
    private static WorldModifier internal;

    private WorldModifier weOrFawe() {
        if (faweEnabled) {
            if (fawe == null) fawe = new FastAsyncWorldEditUtils(plugin);
            return fawe;
        }
        if (weEnabled) {
            if (we == null) we = new WorldEditUtils(plugin);
            return we;
        }
        return null;
    }

    private WorldModifier internal() {
        if (internal == null) internal = new InternalWorldModifier(plugin);
        return internal;
    }

    public WorldModifier resolve(SchematicPlugin requested) {
        return switch (requested) {
            case WORLD_EDIT -> weOrFawe();
            case INTERNAL -> internal();
            case UNKNOWN -> {
                WorldModifier modifier = weOrFawe();
                if (modifier != null) {
                    yield modifier;
                } else {
                    yield internal();
                }
            }
        };
    }

}
