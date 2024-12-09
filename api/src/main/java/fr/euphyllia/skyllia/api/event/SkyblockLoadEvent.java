package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event triggered when a Skyblock island is loaded.
 * <p>
 * This event is called asynchronously when a Skyblock island is loaded into memory,
 * such as during server startup or when an island is accessed for the first time.
 * It allows plugins to perform actions related to island loading, such as initializing
 * island data, setting up configurations, or notifying players.
 * </p>
 * <p>
 * Note: This event is not cancellable. Plugins can listen to this event to execute
 * custom logic upon island loading but cannot prevent the island from being loaded.
 * </p>
 * <p>
 * To handle this event, plugins must register an event listener and implement the appropriate handler.
 * </p>
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * import fr.euphyllia.skyllia.api.event.SkyblockLoadEvent;
 * import fr.euphyllia.skyllia.api.skyblock.Island;
 * import org.bukkit.event.EventHandler;
 * import org.bukkit.event.Listener;
 *
 * public class IslandLoadListener implements Listener {
 *
 *     @EventHandler
 *     public void onIslandLoad(SkyblockLoadEvent event) {
 *         Island island = event.getIsland();
 *
 *         // Example 1: Logging the island load event
 *         System.out.println("Island '" + island.getName() + "' has been loaded.");
 *
 *         // Example 2: Initializing custom data for the island
 *         initializeIslandData(island);
 *
 *         // Example 3: Sending a notification to the island owner (assuming the owner is online)
 *         Player owner = Bukkit.getPlayer(island.getOwnerId());
 *         if (owner != null && owner.isOnline()) {
 *             owner.sendMessage("Your Skyblock island '" + island.getName() + "' has been successfully loaded!");
 *         }
 *     }
 *
 *     /**
 *      * Initializes custom data or configurations for the loaded island.
 *      *
 *      * @param island The island that has been loaded.
 *      *\/
 *     private void initializeIslandData(Island island) {
 *         // Implement custom initialization logic here
 *         // For example, setting up island-specific settings or loading additional data
 *     }
 * }
 * }</pre>
 *
 * @see Island
 */
public class SkyblockLoadEvent extends Event {

    /**
     * The handler list for this event.
     */
    private static final HandlerList handlerList = new HandlerList();

    /**
     * The island that is being loaded.
     */
    private final Island island;

    /**
     * Constructs a new {@code SkyblockLoadEvent}.
     *
     * @param islandCreate The island that is being loaded.
     */
    public SkyblockLoadEvent(Island islandCreate) {
        super(true);
        this.island = islandCreate;
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
     * Retrieves the island that is being loaded.
     *
     * @return The island.
     */
    public Island getIsland() {
        return this.island;
    }
}
