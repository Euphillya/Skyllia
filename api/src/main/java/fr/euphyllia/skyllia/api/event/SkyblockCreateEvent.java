package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Called when a Skyblock island is created. <br />
 */
public class SkyblockCreateEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();
    private final Island island;
    private final UUID owner;

    /**
     * Constructs a new SkyblockCreateEvent.
     *
     * @param islandCreate The island that is being created.
     * @param owner The UUID of the owner of the island.
     */
    public SkyblockCreateEvent(Island islandCreate, UUID owner) {
        super(true);
        this.island = islandCreate;
        this.owner = owner;
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
     * Gets the island that is being created.
     *
     * @return The island.
     */
    public Island getIsland() {
        return this.island;
    }

    /**
     * Gets the UUID of the owner of the island.
     *
     * @return The owner's UUID.
     */
    public UUID getOwnerId() {
        return this.owner;
    }
}
