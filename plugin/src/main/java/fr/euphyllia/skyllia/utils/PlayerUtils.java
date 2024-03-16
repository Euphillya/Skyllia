package fr.euphyllia.skyllia.utils;

import fr.euphyllia.energie.model.SchedulerType;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.event.players.PlayerTeleportSpawnEvent;
import fr.euphyllia.skyllia.api.utils.SupportSpigot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerUtils {

    public static void teleportPlayerSpawn(Player player) {
        SkylliaAPI.getScheduler()
                .runTask(SchedulerType.SYNC, player, schedulerTask -> {
                    PlayerTeleportSpawnEvent playerTeleportSpawnEvent = new PlayerTeleportSpawnEvent(player, Bukkit.getWorlds().get(0).getSpawnLocation());
                    Bukkit.getPluginManager().callEvent(playerTeleportSpawnEvent);
                    if (playerTeleportSpawnEvent.isCancelled()) {
                        return;
                    }
                    SupportSpigot.asyncTeleportEntity(player, playerTeleportSpawnEvent.getFinalLocation());
                }, null);
    }
}
