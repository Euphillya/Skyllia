package fr.euphyllia.skyllia.listeners.permissions.flags.entity;

import fr.euphyllia.skyllia.api.permissions.FlagId;
import fr.euphyllia.skyllia.api.permissions.IslandFlagRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.FlagModule;
import org.bukkit.plugin.Plugin;

public class IslandMobSpawnPassiveFlag implements FlagModule {

    private FlagId ALLOW_ALL_PASSIVE; // Il passe en priorité, si true, tout est true, sinon c'est chacun
    private FlagId ALLOW_SPAWN_ALLAY;
    private FlagId ALLOW_SPAWN_ARMADILLO;
    private FlagId ALLOW_SPAWN_AXOLOTL;
    private FlagId ALLOW_SPAWN_BAT;
    private FlagId ALLOW_SPAWN_CAMEL;
    private FlagId ALLOW_SPAWN_CAT;
    private FlagId ALLOW_SPAWN_CHICKEN;
    private FlagId ALLOW_SPAWN_COD;
    private FlagId ALLOW_SPAWN_COPPERGOLEM;
    private FlagId ALLOW_SPAWN_COW;
    private FlagId ALLOW_SPAWN_DONKEY;
    private FlagId ALLOW_SPAWN_FROG;
    private FlagId ALLOW_SPAWN_GLOWSQUID;
    private FlagId ALLOW_SPAWN_HAPPYGHAST;
    private FlagId ALLOW_SPAWN_HORSE;
    private FlagId ALLOW_SPAWN_MOOSHROOM;
    private FlagId ALLOW_SPAWN_MULE;
    private FlagId ALLOW_SPAWN_OCELOT;
    private FlagId ALLOW_SPAWN_PARROT;
    private FlagId ALLOW_SPAWN_PIG;
    private FlagId ALLOW_SPAWN_RABBIT;
    private FlagId ALLOW_SPAWN_SALMON;
    private FlagId ALLOW_SPAWN_SHEEP;
    private FlagId ALLOW_SPAWN_SNIFFER;
    private FlagId ALLOW_SPAWN_SNOWGOLEM;
    private FlagId ALLOW_SPAWN_SQUID;
    private FlagId ALLOW_SPAWN_STRIDER;
    private FlagId ALLOW_SPAWN_TADPOLE;
    private FlagId ALLOW_SPAWN_TROPICALFISH;
    private FlagId ALLOW_SPAWN_TURTLE;
    private FlagId ALLOW_SPAWN_VILLAGER;
    private FlagId ALLOW_SPAWN_WANDERINGVILLAGER;


    @Override
    public void registerFlags(IslandFlagRegistry registry, Plugin owner) {

    }
}
