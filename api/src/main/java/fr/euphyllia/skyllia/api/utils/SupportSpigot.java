package fr.euphyllia.skyllia.api.utils;

import fr.euphyllia.energie.model.SchedulerType;
import fr.euphyllia.energie.utils.EntityUtils;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;

public class SupportSpigot {

    public static void asyncTeleportEntity(Entity entity, Location location) {
        SkylliaAPI.getScheduler().runTask(SchedulerType.SYNC, entity, schedulerTaskInter -> {
            EntityUtils.teleportAsync(entity, location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }, null);
    }
}
