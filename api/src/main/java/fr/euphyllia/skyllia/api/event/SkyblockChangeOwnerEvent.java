package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents an event that is triggered when the owner of a Skyblock island changes.
 * <p>
 * This event is called when an island changes ownership, allowing plugins to listen for and react to this change.
 * </p>
 * <p>
 * Handlers must register as listeners for this event to be notified of ownership changes.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * import fr.euphyllia.skyllia.api.event.SkyblockChangeOwnerEvent;
 * import org.bukkit.event.EventHandler;
 * import org.bukkit.event.Listener;
 * import java.util.UUID;
 *
 * public class OwnerChangeListener implements Listener {
 *
 *     @EventHandler
 *     public void onOwnerChange(SkyblockChangeOwnerEvent event) {
 *         UUID previousOwner = event.getPreviousOwner();
 *         UUID newOwner = event.getNewOwner();
 *         Island island = event.getIsland();
 *         // Additional logic here
 *     }
 * }
 * }</pre>
 *
 * @see Island
 */
public class SkyblockChangeOwnerEvent extends Event {

    /**
     * The handler list for this event.
     */
    private static final HandlerList handlerList = new HandlerList();

    /**
     * The island whose ownership is changing.
     */
    private final Island island;

    /**
     * UUID of the previous owner of the island.
     */
    private final UUID previousOwner;

    /**
     * UUID of the new owner of the island.
     */
    private final UUID newOwner;

    /**
     * Constructs a new SkyblockChangeOwnerEvent.
     *
     * @param island        The island whose owner is changing.
     * @param previousOwner UUID of the previous owner.
     * @param newOwner      UUID of the new owner.
     */
    public SkyblockChangeOwnerEvent(Island island, UUID previousOwner, UUID newOwner) {
        super(true);
        this.island = island;
        this.previousOwner = previousOwner;
        this.newOwner = newOwner;
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
     * Gets the island whose ownership is changing.
     *
     * @return The island.
     */
    public Island getIsland() {
        return island;
    }

    /**
     * Gets the UUID of the new owner of the island.
     *
     * @return UUID of the new owner.
     */
    public UUID getNewOwner() {
        return newOwner;
    }

    /**
     * Gets the UUID of the previous owner of the island.
     *
     * @return UUID of the previous owner.
     */
    public UUID getPreviousOwner() {
        return previousOwner;
    }
}
