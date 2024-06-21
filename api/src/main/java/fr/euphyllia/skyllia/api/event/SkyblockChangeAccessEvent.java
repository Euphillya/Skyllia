package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when an island's access is changed (opened or closed). <br />
 * This can be prevented by another plugin. <br />
 */
public class SkyblockChangeAccessEvent extends Event implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();
    private final Island island;
    private boolean cancel = false;

    /**
     * Constructs a new SkyblockChangeAccessEvent.
     *
     * @param island The island whose access is being changed.
     */
    public SkyblockChangeAccessEvent(Island island) {
        super(true);
        this.island = island;
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
     * Gets the island whose access is being changed.
     *
     * @return The island.
     */
    public Island getIsland() {
        return this.island;
    }

    /**
     * Gets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins.
     *
     * @return true if this event is cancelled.
     */
    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    /**
     * Sets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins.
     *
     * @param cancel true if you wish to cancel this event.
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
