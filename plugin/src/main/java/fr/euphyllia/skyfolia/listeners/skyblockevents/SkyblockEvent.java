package fr.euphyllia.skyfolia.listeners.skyblockevents;

import fr.euphyllia.skyfolia.api.InterneAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.Listener;

public class SkyblockEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(SkyblockEvent.class);

    public SkyblockEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

}
