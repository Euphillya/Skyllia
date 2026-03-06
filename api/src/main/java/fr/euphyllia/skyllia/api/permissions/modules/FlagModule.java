package fr.euphyllia.skyllia.api.permissions.modules;

import fr.euphyllia.skyllia.api.permissions.IslandFlagRegistry;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public interface FlagModule extends Listener {

    void registerFlags(IslandFlagRegistry registry, Plugin owner);
}
