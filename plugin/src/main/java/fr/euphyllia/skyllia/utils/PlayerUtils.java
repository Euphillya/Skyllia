package fr.euphyllia.skyllia.utils;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.event.players.PlayerTeleportSpawnEvent;
import fr.euphyllia.skyllia.api.hooks.SpawnHook;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerUtils {

    public static void teleportPlayerSpawn(Player player) {
        if (!ConfigLoader.general.isSpawnEnabled()) return;
        player.getScheduler().execute(SkylliaAPI.getPlugin(), () -> {
            if (!player.isOnline()) return;

            SpawnHook spawnHook = Skyllia.getInstance().getInterneAPI().getSpawnHook();
            if (spawnHook != null && spawnHook.isAvailable()) {
                Location hookLocation = spawnHook.getSpawnLocation(player);
                if (hookLocation != null) {
                    PlayerTeleportSpawnEvent playerTeleportSpawnEvent = new PlayerTeleportSpawnEvent(player, hookLocation);
                    Bukkit.getPluginManager().callEvent(playerTeleportSpawnEvent);
                    if (playerTeleportSpawnEvent.isCancelled()) {
                        return;
                    }
                    player.teleportAsync(playerTeleportSpawnEvent.getFinalLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                    return;
                }
            }

            Location location = ConfigLoader.general.getSpawnLocation();
            if (location == null) location = Bukkit.getWorlds().getFirst().getSpawnLocation();
            PlayerTeleportSpawnEvent playerTeleportSpawnEvent = new PlayerTeleportSpawnEvent(player, location);
            Bukkit.getPluginManager().callEvent(playerTeleportSpawnEvent);
            if (playerTeleportSpawnEvent.isCancelled()) {
                return;
            }
            player.teleportAsync(playerTeleportSpawnEvent.getFinalLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        }, null, 1L);

    }
}
