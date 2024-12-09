package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event triggered when a Skyblock island attempts to create a new warp.
 * <p>
 * This event is called asynchronously when a plugin initiates the creation of a new warp
 * on a Skyblock island. The event is not triggered if the warp name is "home", and its execution
 * can be prevented by other plugins by cancelling the event. This allows for additional validation
 * or restrictions on warp creation.
 * </p>
 * <p>
 * To handle this event, plugins must register an event listener and implement the appropriate handler.
 * </p>
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * import fr.euphyllia.skyllia.api.event.SkyblockCreateWarpEvent;
 * import fr.euphyllia.skyllia.api.skyblock.Island;
 * import org.bukkit.Location;
 * import org.bukkit.event.EventHandler;
 * import org.bukkit.event.Listener;
 *
 * public class WarpCreationListener implements Listener {
 *
 *     @EventHandler
 *     public void onWarpCreate(SkyblockCreateWarpEvent event) {
 *         Island island = event.getIsland();
 *         String warpName = event.getWarpName();
 *         Location warpLocation = event.getWarpLocation();
 *
 *         // Example 1: Preventing creation of warps with prohibited names
 *         if (warpName.equalsIgnoreCase("forbiddenWarp")) {
 *             event.setCancelled(true);
 *             System.out.println("Warp creation cancelled: '" + warpName + "' is a prohibited name.");
 *             return;
 *         }
 *
 *         // Example 2: Modifying the warp location based on custom logic
 *         if (warpName.equalsIgnoreCase("customWarp")) {
 *             Location newLocation = adjustWarpLocation(warpLocation);
 *             event.setWarpLocation(newLocation);
 *             System.out.println("Warp location for '" + warpName + "' has been adjusted.");
 *         }
 *
 *         // Example 3: Logging warp creation attempts
 *         System.out.println("Attempting to create warp '" + warpName + "' at location " + warpLocation + " on island '" + island.getName() + "'.");
 *     }
 *
 *     /**
 *      * Adjusts the warp location based on custom logic.
 *      *
 *      * @param originalLocation The original location of the warp.
 *      * @return The adjusted location.
 *      *\/
 *     private Location adjustWarpLocation(Location originalLocation) {
 *         // Implement custom location adjustment logic here
 *         // For example, offset the location by certain coordinates
 *         return originalLocation.clone().add(10, 0, 10);
 *     }
 * }
 * }</pre>
 *
 * @see Island
 * @see Location
 */
public class SkyblockCreateWarpEvent extends Event implements Cancellable {

    /**
     * The handler list for this event.
     */
    private static final HandlerList handlerList = new HandlerList();

    /**
     * The island that is attempting to create the warp.
     */
    private final Island island;

    /**
     * The name of the warp being created.
     */
    private String warp;

    /**
     * The location of the warp being created.
     */
    private Location location;

    /**
     * Indicates whether this event has been cancelled.
     */
    private boolean cancel = false;

    /**
     * Constructs a new {@code SkyblockCreateWarpEvent}.
     *
     * @param island        The island creating the warp.
     * @param warpName      The name of the warp.
     * @param warpLocation  The location of the warp.
     */
    public SkyblockCreateWarpEvent(Island island, String warpName, Location warpLocation) {
        super(true);
        this.island = island;
        this.warp = warpName;
        this.location = warpLocation;
    }

    /**
     * Retrieves the handler list for this event.
     *
     * @return The handler list.
     */
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    /**
     * Retrieves the handlers associated with this event.
     *
     * @return The handler list.
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }

    /**
     * Retrieves the island that is attempting to create the warp.
     *
     * @return The island.
     */
    public Island getIsland() {
        return this.island;
    }

    /**
     * Retrieves the name of the warp being created.
     *
     * @return The warp name.
     */
    public String getWarpName() {
        return this.warp;
    }

    /**
     * Sets a new name for the warp being created.
     *
     * @param warp The new warp name.
     */
    public void setWarpName(String warp) {
        this.warp = warp;
    }

    /**
     * Retrieves the location of the warp being created.
     *
     * @return The warp location.
     */
    public Location getWarpLocation() {
        return this.location;
    }

    /**
     * Sets a new location for the warp being created.
     *
     * @param location The new warp location.
     */
    public void setWarpLocation(Location location) {
        this.location = location;
    }

    /**
     * Checks whether this event has been cancelled. A cancelled event will not be executed on the server,
     * but will still pass to other plugins.
     *
     * @return {@code true} if this event is cancelled, {@code false} otherwise.
     */
    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    /**
     * Sets the cancellation state of this event. A cancelled event will not be executed on the server,
     * but will still pass to other plugins.
     *
     * @param cancel {@code true} to cancel this event, {@code false} to allow it to proceed.
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
