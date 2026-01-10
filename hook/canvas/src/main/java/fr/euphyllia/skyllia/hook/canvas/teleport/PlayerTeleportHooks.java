package fr.euphyllia.skyllia.hook.canvas.teleport;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.event.teleport.PlayerTeleportIslandEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import io.canvasmc.canvas.event.EntityTeleportAsyncEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerTeleportHooks implements Listener {

    @EventHandler
    public void onEntityTeleport(final EntityTeleportAsyncEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Location to = event.getTo();
        if (!SkylliaAPI.isWorldSkyblock(to.getWorld())) return;

        Island island = SkylliaAPI.getIslandByChunk(to.getChunk());
        if (island == null) return;

        new PlayerTeleportIslandEvent(
                player,
                event.getFrom(),
                to,
                island,
                event.getCause(),
                event.isCancelled(),
                true
        ).callEvent();
    }

}
