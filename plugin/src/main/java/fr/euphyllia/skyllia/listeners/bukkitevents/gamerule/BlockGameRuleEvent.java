package fr.euphyllia.skyllia.listeners.bukkitevents.gamerule;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.skyblock.model.gamerule.GameRuleIsland;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockSpreadEvent;

public class BlockGameRuleEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(BlockGameRuleEvent.class);

    public BlockGameRuleEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFireSpreadingBlock(BlockSpreadEvent event) {
        if (event.getNewState().getType().equals(Material.FIRE)) {
            ListenersUtils.checkGameRuleIsland(event.getBlock().getLocation(), GameRuleIsland.DISABLE_FIRE_SPREADING, event);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFireSpread(BlockBurnEvent event) {
        ListenersUtils.checkGameRuleIsland(event.getBlock().getLocation(), GameRuleIsland.DISABLE_FIRE_SPREADING, event);
    }
}
