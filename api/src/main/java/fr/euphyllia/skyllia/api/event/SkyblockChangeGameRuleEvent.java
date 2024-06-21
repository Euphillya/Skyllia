package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is called when a game rule is changed on a Skyblock island.
 */
public class SkyblockChangeGameRuleEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();
    private final Island island;
    private final long gamerule;

    /**
     * Constructs a new SkyblockChangeGameRuleEvent.
     *
     * @param island The island where the game rule is being changed.
     * @param gamerules The new value of the game rule.
     */
    public SkyblockChangeGameRuleEvent(Island island, long gamerules) {
        super(true);
        this.island = island;
        this.gamerule = gamerules;
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
     * Gets the island where the game rule is being changed.
     *
     * @return The island.
     */
    public Island getIsland() {
        return this.island;
    }

    /**
     * Gets the new value of the game rule.
     *
     * @return The new game rule value.
     */
    public long getGamerule() {
        return this.gamerule;
    }
}
