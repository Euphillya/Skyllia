package fr.euphyllia.skyllia.listeners.permissions.flags.entity;

import fr.euphyllia.skyllia.api.permissions.FlagId;
import fr.euphyllia.skyllia.api.permissions.IslandFlagRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.FlagModule;
import org.bukkit.plugin.Plugin;

public class IslandMobSpawnHostileAdjacentFlag implements FlagModule {

    private FlagId ALLOW_SPAWN_ALL_HOSTILE_ADJACENT;
    private FlagId ALLOW_SPAWN_CAMEL_HUSK;
    private FlagId ALLOW_SPAWN_SKELETON_HORSE;
    private FlagId ALLOW_SPAWN_ZOMBIE_HORSE;

    @Override
    public void registerFlags(IslandFlagRegistry registry, Plugin owner) {

    }
}
