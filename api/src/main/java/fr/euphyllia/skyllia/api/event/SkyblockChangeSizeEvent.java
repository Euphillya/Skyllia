package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event triggered when the size of a Skyblock island is changed.
 * <p>
 * This event is called when a plugin modifies the size (radius) of a Skyblock island.
 * It allows other plugins to react to size changes, such as updating related structures,
 * enforcing size constraints, or logging the change.
 * </p>
 * <p>
 * To handle this event, plugins must register an event listener and implement the appropriate handler.
 * </p>
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * import fr.euphyllia.skyllia.api.event.SkyblockChangeSizeEvent;
 * import fr.euphyllia.skyllia.api.skyblock.Island;
 * import org.bukkit.event.EventHandler;
 * import org.bukkit.event.Listener;
 *
 * public class IslandSizeChangeListener implements Listener {
 *
 *     @EventHandler
 *     public void onIslandSizeChange(SkyblockChangeSizeEvent event) {
 *         Island island = event.getIsland();
 *         double newSize = event.getSizeIsland();
 *
 *         // Example 1: Logging the size change
 *         System.out.println("Island '" + island.getName() + "' size changed to radius: " + newSize);
 *
 *         // Example 2: Enforcing a maximum size limit
 *         double maxSize = 100.0;
 *         if (newSize > maxSize) {
 *             System.out.println("Requested size exceeds the maximum allowed. Reverting to max size.");
 *             // If the event were cancellable or allowed modification, you could adjust the size here.
 *             // Note: This event is not cancellable in its current implementation.
 *         }
 *
 *         // Example 3: Updating related structures based on the new size
 *         island.updateStructures(newSize);
 *     }
 * }
 * }</pre>
 *
 * @see Island
 */
public class SkyblockChangeSizeEvent extends Event {

    /**
     * The handler list for this event.
     */
    private static final HandlerList handlerList = new HandlerList();

    /**
     * The island whose size is being changed.
     */
    private final Island island;

    /**
     * The new size (radius) of the island.
     */
    private final double sizeIsland;

    /**
     * Constructs a new {@code SkyblockChangeSizeEvent}.
     *
     * @param island The island whose size is being changed.
     * @param sizeIsland The new radius of the island.
     */
    public SkyblockChangeSizeEvent(Island island, double sizeIsland) {
        super(true);
        this.island = island;
        this.sizeIsland = sizeIsland;
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
     * Retrieves the island whose size is being changed.
     *
     * @return The island.
     */
    public Island getIsland() {
        return this.island;
    }

    /**
     * Retrieves the new size (radius) of the island.
     *
     * @return The new size of the island.
     */
    public double getSizeIsland() {
        return this.sizeIsland;
    }
}
