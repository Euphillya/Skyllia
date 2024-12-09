package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event triggered when a Skyblock island is about to delete a warp.
 * <p>
 * This event is called asynchronously when a plugin initiates the deletion of a warp on a Skyblock island.
 * It allows other plugins to perform actions before the warp is deleted, such as validating the deletion,
 * notifying players, or enforcing restrictions. Additionally, this event can be cancelled by other plugins
 * to prevent the deletion from occurring.
 * </p>
 * <p>
 * Note: This event will not be triggered if the warp name is "home", as "home" warps are handled differently.
 * </p>
 * <p>
 * To handle this event, plugins must register an event listener and implement the appropriate handler.
 * </p>
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * import fr.euphyllia.skyllia.api.event.SkyblockDeleteWarpEvent;
 * import fr.euphyllia.skyllia.api.skyblock.Island;
 * import org.bukkit.event.EventHandler;
 * import org.bukkit.event.Listener;
 * import org.bukkit.entity.Player;
 *
 * public class WarpDeletionListener implements Listener {
 *
 *     @EventHandler
 *     public void onWarpDelete(SkyblockDeleteWarpEvent event) {
 *         Island island = event.getIsland();
 *         String warpName = event.getWarpName();
 *
 *         // Example 1: Prevent deletion of critical warps
 *         if (warpName.equalsIgnoreCase("spawn") || warpName.equalsIgnoreCase("market")) {
 *             event.setCancelled(true);
 *             Player owner = island.getOwner();
 *             if (owner != null && owner.isOnline()) {
 *                 owner.sendMessage("You cannot delete the '" + warpName + "' warp.");
 *             }
 *             return;
 *         }
 *
 *         // Example 2: Log the warp deletion attempt
 *         System.out.println("Attempting to delete warp '" + warpName + "' on island '" + island.getName() + "'.");
 *
 *         // Example 3: Perform additional cleanup tasks
 *         performCleanup(island, warpName);
 *     }
 *
 *     /**
 *      * Performs necessary cleanup tasks after a warp is deleted.
 *      *
 *      * @param island   The island from which the warp is deleted.
 *      * @param warpName The name of the warp being deleted.
 *      *\/
 *     private void performCleanup(Island island, String warpName) {
 *         // Implement cleanup logic here, such as removing warp-specific data or reverting configurations
 *     }
 * }
 * }</pre>
 *
 * @see Island
 */
public class SkyblockDeleteWarpEvent extends Event implements Cancellable {

    /**
     * The handler list for this event.
     */
    private static final HandlerList handlerList = new HandlerList();

    /**
     * The island that is deleting the warp.
     */
    private final Island island;

    /**
     * The name of the warp being deleted.
     */
    private final String warp;

    /**
     * Indicates whether this event has been cancelled.
     */
    private boolean cancel = false;

    /**
     * Constructs a new {@code SkyblockDeleteWarpEvent}.
     *
     * @param island   The island that is deleting the warp.
     * @param warpName The name of the warp being deleted.
     */
    public SkyblockDeleteWarpEvent(Island island, String warpName) {
        super(true);
        this.island = island;
        this.warp = warpName;
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
     * Retrieves the island that is deleting the warp.
     *
     * @return The island.
     */
    public Island getIsland() {
        return this.island;
    }

    /**
     * Retrieves the name of the warp being deleted.
     *
     * @return The warp name.
     */
    public String getWarpName() {
        return this.warp;
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
