package fr.euphyllia.skyllia.utils;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.event.players.PlayerTeleportSpawnEvent;
import fr.euphyllia.skyllia.api.utils.scheduler.SchedulerTask;
import fr.euphyllia.skyllia.api.utils.scheduler.model.SchedulerType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerUtils {

    public static void teleportPlayerSpawn(Main main, Player player) {
        SkylliaAPI.getSchedulerTask().getScheduler(SchedulerTask.SchedulerSoft.MINECRAFT)
                .execute(SchedulerType.ENTITY, player, schedulerTask -> {
                    PlayerTeleportSpawnEvent playerTeleportSpawnEvent = new PlayerTeleportSpawnEvent(player, Bukkit.getWorlds().get(0).getSpawnLocation());
                    Bukkit.getPluginManager().callEvent(playerTeleportSpawnEvent);
                    if (playerTeleportSpawnEvent.isCancelled()) {
                        return;
                    }
                    player.teleportAsync(playerTeleportSpawnEvent.getFinalLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                });
    }
}
