package fr.euphyllia.skyllia.listeners.bukkitevents.paper;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.event.players.PlayerPrepareChangeWorldSkyblockEvent;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PortalAlternativePaperEvent implements Listener {

    private final Logger logger = LogManager.getLogger(PortalAlternativePaperEvent.class);

    public PortalAlternativePaperEvent() {

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInsidePortal(final EntityInsideBlockEvent event) {
        if (event.isCancelled()) return;
        if (event.getEntity() instanceof Player player) {
            Block block = event.getBlock();
            World world = block.getWorld();
            if (!SkylliaAPI.isWorldSkyblock(world)) return;
            event.setCancelled(true);
            Material blockType = block.getType();
            switch (blockType) {
                case NETHER_PORTAL -> {
                    ListenersUtils.callPlayerPrepareChangeWorldSkyblockEvent(player, PlayerPrepareChangeWorldSkyblockEvent.PortalType.NETHER, world.getName());
                }
                case END_PORTAL ->
                        ListenersUtils.callPlayerPrepareChangeWorldSkyblockEvent(player, PlayerPrepareChangeWorldSkyblockEvent.PortalType.END, world.getName());
            }
        }
    }
}
