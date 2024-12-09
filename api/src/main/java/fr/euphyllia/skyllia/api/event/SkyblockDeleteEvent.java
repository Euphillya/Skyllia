package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event triggered when a Skyblock island is about to be deleted.
 * <p>
 * This event is called asynchronously when a plugin initiates the deletion of a Skyblock island.
 * It allows other plugins to perform actions before the island is deleted, such as cleaning up resources,
 * notifying players, or enforcing restrictions on island deletion. Additionally, this event can be
 * cancelled by other plugins to prevent the deletion from occurring.
 * </p>
 * <p>
 * Note: This event will not be triggered if the deletion is initiated with specific conditions
 * that exclude certain plugins or scenarios.
 * </p>
 * <p>
 * To handle this event, plugins must register an event listener and implement the appropriate handler.
 * </p>
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * import fr.euphyllia.skyllia.api.event.SkyblockDeleteEvent;
 * import fr.euphyllia.skyllia.api.skyblock.Island;
 * import org.bukkit.event.EventHandler;
 * import org.bukkit.event.Listener;
 * import org.bukkit.entity.Player;
 *
 * public class IslandDeletionListener implements Listener {
 *
 *     @EventHandler
 *     public void onIslandDelete(SkyblockDeleteEvent event) {
 *         Island island = event.getIsland();
 *
 *         // Example 1: Prevent deletion if the island has active members
 *         if (island.hasActiveMembers()) {
 *             event.setCancelled(true);
 *             Player owner = island.getOwner();
 *             if (owner != null && owner.isOnline()) {
 *                 owner.sendMessage("Your island cannot be deleted because it has active members.");
 *             }
 *             return;
 *         }
 *
 *         // Example 2: Perform cleanup tasks before deletion
 *         performCleanup(island);
 *
 *         // Example 3: Log the deletion event
 *         System.out.println("Island '" + island.getName() + "' owned by " + island.getOwnerName() + " has been deleted.");
 *     }
 *
 *     /**
 *      * Performs necessary cleanup tasks before an island is deleted.
 *      *
 *      * @param island The island that is being deleted.
 *      *\/
 *     private void performCleanup(Island island) {
 *         // Implement cleanup logic here, such as removing custom data or reverting configurations
 *     }
 * }
 * }</pre>
 *
 * @see Island
 */
public class SkyblockDeleteEvent extends Event implements Cancellable {

    /**
     * The handler list for this event.
     */
    private static final HandlerList handlerList = new HandlerList();

    /**
     * The island that is going to be deleted.
     */
    private final Island island;

    /**
     * Indicates whether this event has been cancelled.
     */
    private boolean cancel = false;

    /**
     * Constructs a new {@code SkyblockDeleteEvent}.
     *
     * @param island The island that is going to be deleted.
     */
    public SkyblockDeleteEvent(Island island) {
        super(true);
        this.island = island;
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
     * Retrieves the island that is going to be deleted.
     *
     * @return The island.
     */
    public Island getIsland() {
        return this.island;
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
