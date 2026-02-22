package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Fired (async) when a new Skyblock island has been registered in the
 * database and is ready, but <em>before</em> any schematic has been pasted.
 *
 * <p>Listen to {@link SkyblockWorldInitEvent} if you need the exact world
 * location and schematic settings for each world of the island.</p>
 */
public class SkyblockCreateEvent extends IslandEvent {

    /**
     * The handler list for this event.
     */
    private static final HandlerList handlerList = new HandlerList();

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
        super(island, true);
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
     * Retrieves the UUID of the owner of the island.
     *
     * @return The owner's UUID.
     */
    public UUID getOwnerId() {
        return this.owner;
    }
}
