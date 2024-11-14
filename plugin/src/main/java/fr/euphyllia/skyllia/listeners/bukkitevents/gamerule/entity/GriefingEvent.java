package fr.euphyllia.skyllia.listeners.bukkitevents.gamerule.entity;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.skyblock.model.gamerule.GameRuleIsland;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class GriefingEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(GriefingEvent.class);

    public GriefingEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onGriefingBlock(final EntityChangeBlockEvent event) {
        if (event.isCancelled()) return;
        Entity entity = event.getEntity();
        Location location = event.getBlock().getLocation();
        if (entity instanceof Enderman) {
            ListenersUtils.checkGameRuleIsland(location, GameRuleIsland.DISABLE_ENDERMAN_PICK_BLOCK, event);
        } else if (entity instanceof AbstractVillager || entity instanceof Animals) {
            ListenersUtils.checkGameRuleIsland(location, GameRuleIsland.DISABLE_PASSIF_MOB_GRIEFING, event);
        } else if (entity instanceof Monster) {
            ListenersUtils.checkGameRuleIsland(location, GameRuleIsland.DISABLE_HOSTILE_MOB_GRIEFING, event);
        } else if (entity instanceof Player) {
            ListenersUtils.checkGameRuleIsland(location, GameRuleIsland.DISABLE_PLAYER_GRIEFING, event);
        } else {
            ListenersUtils.checkGameRuleIsland(location, GameRuleIsland.DISABLE_UNKNOWN_MOB_GRIEFING, event);
        }
    }
}

