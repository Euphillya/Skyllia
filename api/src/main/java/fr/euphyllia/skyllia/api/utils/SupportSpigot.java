package fr.euphyllia.skyllia.api.utils;

import fr.euphyllia.energie.model.SchedulerType;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;

public class SupportSpigot {

    public static void asyncTeleportEntity(Entity entity, Location location) {
        SkylliaAPI.getScheduler().runTask(SchedulerType.SYNC, entity, schedulerTaskInter -> {
            if (PaperLib.isSpigot()) {
                PaperLib.teleportAsync(entity, location, PlayerTeleportEvent.TeleportCause.PLUGIN);
            } else {
                entity.teleportAsync(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
        }, null);
    }
}
