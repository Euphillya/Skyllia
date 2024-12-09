package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.model.IslandSettings;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents an event that is fired when a Skyblock island is about to be created.
 * <p>
 * This event is triggered before the creation of a new Skyblock island, allowing plugins to modify
 * the island's settings or cancel the creation process. It provides access to the island's unique
 * identifier and its settings, enabling developers to customize the island creation workflow.
 * </p>
 * <p>
 * To handle this event, plugins must register an event listener and implement the appropriate handler.
 * </p>
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * import fr.euphyllia.skyllia.api.event.PrepareSkyblockCreateEvent;
 * import fr.euphyllia.skyllia.api.skyblock.model.IslandSettings;
 * import org.bukkit.event.EventHandler;
 * import org.bukkit.event.Listener;
 * import java.util.UUID;
 *
 * public class IslandCreationListener implements Listener {
 *
 *     @EventHandler
 *     public void onPrepareIslandCreate(PrepareSkyblockCreateEvent event) {
 *         UUID islandId = event.getIslandId();
 *         IslandSettings settings = event.getIslandSettings();
 *
 *         // Example 1: Modify Island Settings Before Creation
 *         settings.setStartingResources(500);
 *         settings.setSpawnProtectionRadius(10);
 *         event.setIslandSettings(settings);
 *
 *         // Example 2: Cancel Island Creation Based on Custom Criteria
 *         if (isIslandIdBlacklisted(islandId)) {
 *             event.setCancelled(true);
 *             // Optionally, notify the player or log the cancellation
 *             System.out.println("Island creation cancelled for ID: " + islandId);
 *         }
 *
 *         // Example 3: Logging Island Creation Attempt
 *         System.out.println("Preparing to create island with ID: " + islandId);
 *     }
 *
 *     /**
 *      * Checks if the provided island ID is blacklisted from being created.
 *      *
 *      * @param islandId The UUID of the island being created.
 *      * @return {@code true} if the island ID is blacklisted, {@code false} otherwise.
 *      *\/
 *     private boolean isIslandIdBlacklisted(UUID islandId) {
 *         // Implement your logic to determine if the island ID should be blacklisted
 *         // For demonstration purposes, let's blacklist a specific UUID
 *         UUID blacklistedId = UUID.fromString("00000000-0000-0000-0000-000000000000");
 *         return islandId.equals(blacklistedId);
 *     }
 * }
 * }</pre>
 *
 * @see IslandSettings
 */
public class PrepareSkyblockCreateEvent extends Event implements Cancellable {

    /**
     * The handler list for this event.
     */
    private static final HandlerList handlerList = new HandlerList();

    /**
     * The unique identifier of the island being created.
     */
    private final UUID id;

    /**
     * The settings of the island being created.
     */
    private IslandSettings islandSettings;

    /**
     * Indicates whether this event is cancelled.
     */
    private boolean cancel = false;

    /**
     * Constructs a new {@code PrepareSkyblockCreateEvent}.
     *
     * @param islandId The UUID of the island being created.
     * @param settings The settings of the island being created.
     */
    public PrepareSkyblockCreateEvent(UUID islandId, IslandSettings settings) {
        super(true);
        this.id = islandId;
        this.islandSettings = settings;
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
     * Retrieves the UUID of the island being created.
     *
     * @return The island's UUID.
     */
    public UUID getIslandId() {
        return this.id;
    }

    /**
     * Retrieves the settings of the island being created.
     *
     * @return The {@link IslandSettings} of the island.
     */
    public IslandSettings getIslandSettings() {
        return this.islandSettings;
    }

    /**
     * Sets new settings for the island being created.
     *
     * @param settings The new {@link IslandSettings} to apply.
     */
    public void setIslandSettings(IslandSettings settings) {
        this.islandSettings = settings;
    }

    /**
     * Checks whether this event has been cancelled.
     * <p>
     * A cancelled event will prevent the island from being created on the server, though the event will still pass to other plugins.
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
     * Cancelling the event will prevent the island from being created on the server, though the event will still pass to other plugins.
     * </p>
     *
     * @param cancel {@code true} to cancel the event, {@code false} to allow it.
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
