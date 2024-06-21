package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a Skyblock island wants to create a new warp. <br />
 * The event is not called if the warp is named "home". <br />
 * This can be prevented by another plugin. <br />
 */
public class SkyblockCreateWarpEvent extends Event implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();
    private final Island island;
    private boolean cancel = false;
    private String warp;
    private Location location;

    /**
     * Constructs a new SkyblockCreateWarpEvent.
     *
     * @param island The island creating the warp.
     * @param warpName The name of the warp.
     * @param warpLocation The location of the warp.
     */
    public SkyblockCreateWarpEvent(Island island, String warpName, Location warpLocation) {
        super(true);
        this.island = island;
        this.warp = warpName;
        this.location = warpLocation;
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
     * Gets the island creating the warp.
     *
     * @return The island.
     */
    public Island getIsland() {
        return this.island;
    }

    /**
     * Gets the name of the warp.
     *
     * @return The warp name.
     */
    public String getWarpName() {
        return this.warp;
    }

    /**
     * Sets the name of the warp.
     *
     * @param warp The new warp name.
     */
    public void setWarpName(String warp) {
        this.warp = warp;
    }

    /**
     * Gets the location of the warp.
     *
     * @return The warp location.
     */
    public Location getWarpLocation() {
        return this.location;
    }

    /**
     * Sets the location of the warp.
     *
     * @param location The new warp location.
     */
    public void setWarpLocation(Location location) {
        this.location = location;
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
