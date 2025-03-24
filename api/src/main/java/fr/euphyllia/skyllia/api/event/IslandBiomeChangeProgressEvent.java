package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called asynchronously during the biome transformation process of an island.
 * <p>
 * This event is dispatched every time a chunk has finished its biome update
 * as part of a complete island-wide biome change operation.
 * It provides real-time feedback about the current progression of the operation.
 * </p>
 *
 * <p>
 * Since this event is asynchronous, any operations that involve
 * the Bukkit API must be carefully synchronized with the main server thread,
 * using {@code Bukkit.getScheduler().runTask(...)} or similar mechanisms.
 * </p>
 *
 * <h2>Usage Examples:</h2>
 * <ul>
 *     <li>Displaying a bossbar to the player showing biome change progression.</li>
 *     <li>Sending periodic chat messages to administrators about the update status.</li>
 *     <li>Tracking analytics or logging how long it takes to update all chunks.</li>
 *     <li>Triggering custom behaviors when the biome transformation reaches a threshold.</li>
 * </ul>
 *
 * <h3>Design Choices:</h3>
 * <ul>
 *     <li>The event is asynchronous to match the background nature of the chunk processing logic.</li>
 *     <li>No percentage/progress ratio is provided: developers are expected to compute it themselves from {@code getRemainingChunks()} and {@code getTotalChunks()}.</li>
 *     <li>The associated island is included to allow filtering or per-island UI.</li>
 * </ul>
 *
 * @see fr.euphyllia.skyllia.api.skyblock.Island
 */
public class IslandBiomeChangeProgressEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Island island;
    private final int remainingChunks;
    private final int totalChunks;

    /**
     * Constructs a new IslandBiomeChangeProgressEvent.
     *
     * @param island          The island currently undergoing a biome transformation.
     * @param remainingChunks The number of chunks still awaiting biome updates.
     * @param totalChunks     The total number of chunks that will be updated in the operation.
     */
    public IslandBiomeChangeProgressEvent(Island island, int remainingChunks, int totalChunks) {
        super(true); // This is an asynchronous event.
        this.island = island;
        this.remainingChunks = remainingChunks;
        this.totalChunks = totalChunks;
    }

    /**
     * Gets the island associated with this biome change progress update.
     *
     * @return The island being modified.
     */
    public Island getIsland() {
        return island;
    }

    /**
     * Gets the number of chunks that still need to be processed.
     *
     * @return Remaining chunk count.
     */
    public int getRemainingChunks() {
        return remainingChunks;
    }

    /**
     * Gets the total number of chunks involved in the biome update.
     *
     * @return Total chunk count.
     */
    public int getTotalChunks() {
        return totalChunks;
    }

    /**
     * Returns the handler list for this event type.
     *
     * @return The handler list.
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Returns the static handler list used by the Bukkit event system.
     *
     * @return The handler list.
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
