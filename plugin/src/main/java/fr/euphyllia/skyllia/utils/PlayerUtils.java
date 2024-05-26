package fr.euphyllia.skyllia.utils;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.event.players.PlayerTeleportSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerUtils {

    public static void teleportPlayerSpawn(Player player) {
        player.getScheduler().execute(SkylliaAPI.getPlugin(), () -> {
            PlayerTeleportSpawnEvent playerTeleportSpawnEvent = new PlayerTeleportSpawnEvent(player, Bukkit.getWorlds().getFirst().getSpawnLocation());
            Bukkit.getPluginManager().callEvent(playerTeleportSpawnEvent);
            if (playerTeleportSpawnEvent.isCancelled()) {
                return;
            }
            player.teleportAsync(playerTeleportSpawnEvent.getFinalLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        }, null, 0L);

    }
}
