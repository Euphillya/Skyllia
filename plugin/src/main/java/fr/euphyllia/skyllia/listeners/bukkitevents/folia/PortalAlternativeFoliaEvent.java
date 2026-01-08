package fr.euphyllia.skyllia.listeners.bukkitevents.folia;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;

public class PortalAlternativeFoliaEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(PortalAlternativeFoliaEvent.class);

    public PortalAlternativeFoliaEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPortalCreate(final PortalCreateEvent event) {
        World world = event.getWorld();
        if (!world.getEnvironment().equals(World.Environment.NORMAL) && WorldUtils.isWorldSkyblock(world.getName())) {
            event.setCancelled(true);
        }
    }
}
