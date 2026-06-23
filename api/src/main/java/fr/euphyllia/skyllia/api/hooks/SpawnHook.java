package fr.euphyllia.skyllia.api.hooks;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface SpawnHook extends PluginHook {

    @Nullable Location getSpawnLocation(Player player);
}
