package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Called when a player uses the <code>/is info</code> command to display island information.
 * <p>
 * This event allows other plugins to append additional lines to the info display
 * using {@link #addLine(Component)}.
 * <p>
 * The event is asynchronous.
 */
public class IslandInfoEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player viewer;
    private final UUID islandId;
    private final Island island;
    private final List<Component> extraMessages = new ArrayList<>();

    /**
     * Constructs a new IslandInfoEvent.
     *
     * @param viewer The player viewing the island information.
     * @param island The island whose information is being displayed.
     */
    public IslandInfoEvent(Player viewer, Island island) {
        super(true); // asynchronous event
        this.viewer = viewer;
        this.island = island;
        this.islandId = island.getId();
    }

    /**
     * Gets the static event handler list.
     *
     * @return The handlers.
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Gets the player who executed the info command.
     *
     * @return The viewer.
     */
    public Player getViewer() {
        return viewer;
    }

    /**
     * Gets the UUID of the island.
     *
     * @return The island ID.
     */
    public UUID getIslandId() {
        return islandId;
    }

    /**
     * Gets the island whose information is being displayed.
     *
     * @return The island.
     */
    public Island getIsland() {
        return island;
    }

    /**
     * Adds a new line of information to be displayed to the player.
     *
     * @param line A {@link Component} to append to the info output.
     */
    public void addLine(Component line) {
        extraMessages.add(line);
    }

    /**
     * Gets the list of extra messages added by other plugins.
     *
     * @return A list of {@link Component}s.
     */
    public List<Component> getExtraMessages() {
        return extraMessages;
    }

    /**
     * Gets the event handler list.
     *
     * @return The handlers.
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
