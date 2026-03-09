package fr.euphyllia.skyllia.listeners.permissions.flags.entity;

import fr.euphyllia.skyllia.api.permissions.FlagId;
import fr.euphyllia.skyllia.api.permissions.IslandFlagRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.FlagModule;
import org.bukkit.plugin.Plugin;

public class IslandMobSpawnNeutralFlag implements FlagModule {

    private FlagId ALLOW_SPAWN_ALL_NEUTRAL;
    private FlagId ALLOW_SPAWN_BEE;
    private FlagId ALLOW_SPAWN_CAVE_SPIDER;
    private FlagId ALLOW_SPAWN_DOLPHIN;
    private FlagId ALLOW_SPAWN_DROWNED;
    private FlagId ALLOW_SPAWN_ENDER_MAN;
    private FlagId ALLOW_SPAWN_FOX;
    private FlagId ALLOW_SPAWN_GOAT;
    private FlagId ALLOW_SPAWN_IRON_GOLEM;
    private FlagId ALLOW_SPAWN_LLAMA;
    private FlagId ALLOW_SPAWN_NAUTILUS;
    private FlagId ALLOW_SPAWN_PANDA;
    private FlagId ALLOW_SPAWN_PIGLIN;
    private FlagId ALLOW_SPAWN_POLAR_BEAR;
    private FlagId ALLOW_SPAWN_PUFFERFISH;
    private FlagId ALLOW_SPAWN_SPIDER;
    private FlagId ALLOW_SPAWN_SPAWN_TRADER_LLAMA;
    private FlagId ALLOW_SPAWN_SPAWN_WOLF;
    private FlagId ALLOW_SPAWN_SPAWN_ZOMBIE_NAUTILUS;
    private FlagId ALLOW_SPAWN_SPAWN_ZOMBIFIED_PIGLIN;

    @Override
    public void registerFlags(IslandFlagRegistry registry, Plugin owner) {

    }
}
