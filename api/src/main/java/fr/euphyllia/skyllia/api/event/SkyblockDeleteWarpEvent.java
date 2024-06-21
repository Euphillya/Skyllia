package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a Skyblock island is going to delete a warp. <br />
 * This can be prevented by another plugin. <br />
 */
public class SkyblockDeleteWarpEvent extends Event implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();
    private final Island island;
    private final String warp;
    private boolean cancel = false;

    /**
     * Constructs a new SkyblockDeleteWarpEvent.
     *
     * @param island The island that is deleting the warp.
     * @param warpName The name of the warp being deleted.
     */
    public SkyblockDeleteWarpEvent(Island island, String warpName) {
        super(true);
        this.island = island;
        this.warp = warpName;
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
     * Gets the name of the warp being deleted.
     *
     * @return The warp name.
     */
    public String getWarpName() {
        return warp;
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
     * Gets the island that is deleting the warp.
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
