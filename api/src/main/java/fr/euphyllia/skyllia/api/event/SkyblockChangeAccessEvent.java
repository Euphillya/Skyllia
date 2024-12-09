package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event that is triggered when the access status of a Skyblock island is changed (opened or closed).
 * <p>
 * This event is fired whenever an island's access level is modified, allowing plugins to perform actions
 * before or after the access change occurs. Additionally, the event can be cancelled to prevent the access
 * change from taking effect, enabling other plugins to override or control access permissions.
 * </p>
 * <p>
 * To handle this event, plugins must register an event listener and implement the appropriate handler.
 * </p>
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * import fr.euphyllia.skyllia.api.event.SkyblockChangeAccessEvent;
 * import fr.euphyllia.skyllia.api.skyblock.Island;
 * import org.bukkit.event.EventHandler;
 * import org.bukkit.event.Listener;
 * import org.bukkit.entity.Player;
 *
 * public class AccessChangeListener implements Listener {
 *
 *     /**
 *      * Handles the SkyblockChangeAccessEvent to monitor and control access changes.
 *      *
 *      * @param event The SkyblockChangeAccessEvent instance.
 *      *\/
 *     @EventHandler
 *     public void onSkyblockChangeAccess(SkyblockChangeAccessEvent event) {
 *         Island island = event.getIsland();
 *
 *         // Example 1: Logging Access Changes
 *         Player owner = island.getOwnerPlayer(); // Assume this method exists
 *         if (owner != null) {
 *             owner.sendMessage("Your island access status is being changed.");
 *         }
 *         System.out.println("Access status changed for island ID: " + island.getId());
 *
 *         // Example 2: Preventing Access Changes for Certain Islands
 *         if (isProtectedIsland(island)) {
 *             event.setCancelled(true);
 *             Player player = getPlayerAttemptingChange(); // Assume this method exists
 *             if (player != null) {
 *                 player.sendMessage("You cannot change access for this protected island.");
 *             }
 *             System.out.println("Access change cancelled for protected island ID: " + island.getId());
 *             return;
 *         }
 *
 *         // Example 3: Modifying Island Settings Before Access Change
 *         // (Assuming Island has methods to modify access settings)
 *         if (shouldAutomaticallyOpen(island)) {
 *             island.setAccessStatus(true); // Automatically open access
 *             System.out.println("Island access automatically set to open for island ID: " + island.getId());
 *         }
 *     }
 *
 *     /**
 *      * Determines if the island is protected and should not allow access changes.
 *      *
 *      * @param island The island to check.
 *      * @return {@code true} if the island is protected, {@code false} otherwise.
 *      *\/
 *     private boolean isProtectedIsland(Island island) {
 *         // Implement your logic to determine if the island is protected
 *         // For demonstration purposes, let's assume islands with even UUIDs are protected
 *         return island.getId().getLeastSignificantBits() % 2 == 0;
 *     }
 *
 *     /**
 *      * Retrieves the player attempting to change the island's access.
 *      *
 *      * @return The player instance, or {@code null} if not applicable.
 *      *\/
 *     private Player getPlayerAttemptingChange() {
 *         // Implement logic to retrieve the player attempting the change
 *         // This might involve accessing the event source or context
 *         return null; // Placeholder implementation
 *     }
 *
 *     /**
 *      * Determines if the island should automatically have its access opened.
 *      *
 *      * @param island The island to check.
 *      * @return {@code true} if access should be automatically opened, {@code false} otherwise.
 *      *\/
 *     private boolean shouldAutomaticallyOpen(Island island) {
 *         // Implement your logic to decide if access should be automatically opened
 *         // For demonstration, let's say islands with more than 5 members should be open
 *         return island.getMemberCount() > 5;
 *     }
 * }
 * }</pre>
 *
 * @see Island
 */
public class SkyblockChangeAccessEvent extends Event implements Cancellable {

    /**
     * The handler list for this event.
     */
    private static final HandlerList handlerList = new HandlerList();

    /**
     * The island whose access status is being changed.
     */
    private final Island island;

    /**
     * Indicates whether this event is cancelled.
     */
    private boolean cancel = false;

    /**
     * Constructs a new {@code SkyblockChangeAccessEvent}.
     *
     * @param island The {@link Island} whose access status is being changed.
     */
    public SkyblockChangeAccessEvent(Island island) {
        super(true);
        this.island = island;
    }

    /**
     * Retrieves the handler list for this event.
     *
     * @return The {@link HandlerList} associated with this event.
     */
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    /**
     * Retrieves the handlers associated with this event.
     *
     * @return The {@link HandlerList} for this event.
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }

    /**
     * Retrieves the {@link Island} whose access status is being changed.
     *
     * @return The {@link Island} involved in the access change.
     */
    public Island getIsland() {
        return this.island;
    }

    /**
     * Checks whether this event has been cancelled.
     * <p>
     * A cancelled event will prevent the access status change from being executed on the server,
     * though the event will still pass to other plugins.
     * </p>
     *
     * @return {@code true} if the event is cancelled, {@code false} otherwise.
     */
    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    /**
     * Sets the cancellation state of this event.
     * <p>
     * Cancelling the event will prevent the access status change from being executed on the server,
     * though the event will still pass to other plugins.
     * </p>
     *
     * @param cancel {@code true} to cancel the event, {@code false} to allow it.
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
