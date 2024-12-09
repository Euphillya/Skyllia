package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents an event triggered when a new Skyblock island is created.
 * <p>
 * This event is called asynchronously when a plugin initiates the creation of a new Skyblock island.
 * It allows other plugins to perform actions related to island creation, such as initializing
 * island data, notifying other players, or applying custom configurations.
 * </p>
 * <p>
 * To handle this event, plugins must register an event listener and implement the appropriate handler.
 * </p>
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * import fr.euphyllia.skyllia.api.event.SkyblockCreateEvent;
 * import fr.euphyllia.skyllia.api.skyblock.Island;
 * import org.bukkit.Bukkit;
 * import org.bukkit.entity.Player;
 * import org.bukkit.event.EventHandler;
 * import org.bukkit.event.Listener;
 *
 * import java.util.UUID;
 *
 * public class IslandCreationListener implements Listener {
 *
 *     @EventHandler
 *     public void onIslandCreate(SkyblockCreateEvent event) {
 *         Island island = event.getIsland();
 *         UUID ownerId = event.getOwnerId();
 *
 *         // Example 1: Logging the creation of a new island
 *         System.out.println("A new island named '" + island.getName() + "' has been created for owner with UUID: " + ownerId);
 *
 *         // Example 2: Initializing island data
 *         initializeIslandData(island);
 *
 *         // Example 3: Sending a welcome message to the island owner (assuming owner is online)
 *         Player owner = Bukkit.getPlayer(ownerId);
 *         if (owner != null && owner.isOnline()) {
 *             owner.sendMessage("Your new Skyblock island '" + island.getName() + "' has been successfully created!");
 *         }
 *     }
 *
 *     /**
 *      * Initializes custom data or structures for the newly created island.
 *      *
 *      * @param island The newly created island.
 *      *\/
 *     private void initializeIslandData(Island island) {
 *         // Implement custom initialization logic here
 *     }
 * }
 * }</pre>
 *
 * @see Island
 */
public class SkyblockCreateEvent extends Event {

    /**
     * The handler list for this event.
     */
    private static final HandlerList handlerList = new HandlerList();

    /**
     * The island that is being created.
     */
    private final Island island;

    /**
     * The UUID of the owner of the island.
     */
    private final UUID owner;

    /**
     * Constructs a new {@code SkyblockCreateEvent}.
     *
     * @param island The island that is being created.
     * @param owner  The UUID of the owner of the island.
     */
    public SkyblockCreateEvent(Island island, UUID owner) {
        super(true);
        this.island = island;
        this.owner = owner;
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
     * Retrieves the island that is being created.
     *
     * @return The island.
     */
    public Island getIsland() {
        return this.island;
    }

    /**
     * Retrieves the UUID of the owner of the island.
     *
     * @return The owner's UUID.
     */
    public UUID getOwnerId() {
        return this.owner;
    }
}
