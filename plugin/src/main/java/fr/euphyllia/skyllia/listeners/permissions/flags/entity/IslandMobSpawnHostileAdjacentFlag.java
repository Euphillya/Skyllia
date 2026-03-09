package fr.euphyllia.skyllia.listeners.permissions.flags.entity;

import fr.euphyllia.skyllia.api.permissions.FlagId;
import fr.euphyllia.skyllia.api.permissions.IslandFlagRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.FlagModule;
import org.bukkit.plugin.Plugin;

public class IslandMobSpawnHostileAdjacentFlag implements FlagModule {

    private FlagId ALLOW_SPAWN_ALL_HOSTILEADJACENT;
    private FlagId ALLOW_SPAWN_CAMELHUSK;
    private FlagId ALLOW_SPAWN_SKELETONHORSE;
    private FlagId ALLOW_SPAWN_ZOMBIEHORSE;

    @Override
    public void registerFlags(IslandFlagRegistry registry, Plugin owner) {

    }
}
