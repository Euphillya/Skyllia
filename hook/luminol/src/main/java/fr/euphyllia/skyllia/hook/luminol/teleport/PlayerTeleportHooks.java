package fr.euphyllia.skyllia.hook.luminol.teleport;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.event.teleport.PlayerTeleportIslandEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import me.earthme.luminol.api.entity.EntityTeleportAsyncEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerTeleportHooks implements Listener {

    @EventHandler
    public void onEntityTeleport(final EntityTeleportAsyncEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Location to = event.getDestination();
        if (!SkylliaAPI.isWorldSkyblock(to.getWorld())) return;

        Island island = SkylliaAPI.getIslandByChunk(to.getChunk());
        if (island == null) return;

        new PlayerTeleportIslandEvent(
                player,
                player.getLocation(),
                to,
                island,
                event.getTeleportCause(),
                false, // Luminol's async events are not cancellable
                true
        ).callEvent();
    }

}
