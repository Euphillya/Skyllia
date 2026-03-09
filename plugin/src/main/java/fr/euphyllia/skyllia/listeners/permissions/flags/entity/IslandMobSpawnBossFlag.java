package fr.euphyllia.skyllia.listeners.permissions.flags.entity;

import fr.euphyllia.skyllia.api.permissions.FlagId;
import fr.euphyllia.skyllia.api.permissions.IslandFlagRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.FlagModule;
import org.bukkit.plugin.Plugin;

public class IslandMobSpawnBossFlag implements FlagModule {

    private FlagId ALLOW_SPAWN_ALL_BOSS;
    private FlagId ALLOW_SPAWN_ENDER_DRAGON;
    private FlagId ALLOW_SPAWN_WITHER;

    @Override
    public void registerFlags(IslandFlagRegistry registry, Plugin owner) {

    }
}
