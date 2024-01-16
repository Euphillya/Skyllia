package fr.euphyllia.skyfolia.listeners.bukkitevents;

import fr.euphyllia.skyfolia.api.InterneAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(BlockEvent.class);

    public BlockEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    public void onBlockBreakOnIsland(final BlockBreakEvent event) {

    }

}
