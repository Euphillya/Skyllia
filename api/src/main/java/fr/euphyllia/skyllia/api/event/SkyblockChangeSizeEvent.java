package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is called when the size of a Skyblock island is changed.
 */
public class SkyblockChangeSizeEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();
    private final Island island;
    private final double sizeIsland;

    /**
     * Constructs a new SkyblockChangeSizeEvent.
     *
     * @param island The island whose size is being changed.
     * @param rayon The new radius of the island.
     */
    public SkyblockChangeSizeEvent(Island island, double rayon) {
        super(true);
        this.island = island;
        this.sizeIsland = rayon;
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
     * Gets the island whose size is being changed.
     *
     * @return The island.
     */
    public Island getIsland() {
        return this.island;
    }

    /**
     * Gets the new size of the island.
     *
     * @return The new size of the island.
     */
    public double getSizeIsland() {
        return this.sizeIsland;
    }
}
