package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a Skyblock island is loaded.
 */
public class SkyblockLoadEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();
    private final Island island;

    /**
     * Constructs a new SkyblockLoadEvent.
     *
     * @param islandCreate The island that is being loaded.
     */
    public SkyblockLoadEvent(Island islandCreate) {
        super(true);
        this.island = islandCreate;
    }

    /**
     * Gets the handler list for this event.
     *
     * @return The handler list.
     */
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    /**
     * Gets the handlers for this event.
     *
     * @return The handlers.
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }

    /**
     * Gets the island that is being loaded.
     *
     * @return The island.
     */
    public Island getIsland() {
        return this.island;
    }
}
