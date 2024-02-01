package fr.euphyllia.skyllia.listeners.bukkitevents.gamerule.entity;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.skyblock.model.gamerule.GameRuleIsland;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class PickupEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(PickupEvent.class);

    public PickupEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPickupItem(final EntityPickupItemEvent event) {
        if (event.isCancelled()) return;
        if (event.getEntity() instanceof Mob) {
            ListenersUtils.checkGameRuleIsland(event.getEntity().getLocation(), GameRuleIsland.MOB_PICKUP_ITEMS, event);
        }
    }

}