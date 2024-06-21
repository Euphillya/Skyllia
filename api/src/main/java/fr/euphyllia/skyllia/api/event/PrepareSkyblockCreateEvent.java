package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.model.IslandSettings;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This event is fired when a Skyblock island is about to be created.
 */
public class PrepareSkyblockCreateEvent extends Event implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();
    private final UUID id;
    private IslandSettings islandSettings;
    private boolean cancel = false;

    /**
     * Constructs a new PrepareSkyblockCreateEvent.
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
     * Gets the UUID of the island being created.
     *
     * @return The island UUID.
     */
    public UUID getIslandId() {
        return this.id;
    }

    /**
     * Gets the settings of the island being created.
     *
     * @return The island settings.
     */
    public IslandSettings getIslandSettings() {
        return this.islandSettings;
    }

    /**
     * Sets the settings of the island being created.
     *
     * @param settings The new island settings.
     */
    public void setIslandSettings(IslandSettings settings) {
        this.islandSettings = settings;
    }

    /**
     * Gets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins.
     *
     * @return true if this event is cancelled.
     */
    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    /**
     * Sets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins.
     *
     * @param cancel true if you wish to cancel this event.
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
