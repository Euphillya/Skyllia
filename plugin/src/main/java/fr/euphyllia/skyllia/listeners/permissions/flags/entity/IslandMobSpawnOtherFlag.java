package fr.euphyllia.skyllia.listeners.permissions.flags.entity;

import fr.euphyllia.skyllia.api.permissions.FlagId;
import fr.euphyllia.skyllia.api.permissions.IslandFlagRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.FlagModule;
import org.bukkit.plugin.Plugin;

public class IslandMobSpawnOtherFlag implements FlagModule {

    private FlagId ALLOW_SPAWN_ALL_OTHER;
    private FlagId ALLOW_SPAWN_GIANT;
    private FlagId ALLOW_SPAWN_ILLUSIONER;
    private FlagId ALLOW_SPAWN_MANNEQUIN;
    private FlagId ALLOW_SPAWN_ARMOR_STAND;

    @Override
    public void registerFlags(IslandFlagRegistry registry, Plugin owner) {

    }
}
