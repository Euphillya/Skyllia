package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.enums.RemovalCause;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a member is removed from a Skyblock island.
 *
 * <p>The {@link RemovalCause} describes why the member was removed:</p>
 * <ul>
 *   <li>{@link RemovalCause#KICKED}         — removed by another player.</li>
 *   <li>{@link RemovalCause#LEAVE}          — left voluntarily.</li>
 *   <li>{@link RemovalCause#ISLAND_DELETED} — the island was deleted.</li>
 * </ul>
 */
public class SkyblockRemoveMemberEvent extends IslandEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Players removedPlayer;
    private final RemovalCause cause;

    /**
     * @param island        The island the player was removed from.
     * @param removedPlayer The player who was removed.
     * @param cause         The reason for the removal.
     */
    public SkyblockRemoveMemberEvent(Island island, Players removedPlayer, RemovalCause cause) {
        super(island, true);
        this.removedPlayer = removedPlayer;
        this.cause = cause;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    /**
     * Returns the player who was removed from the island.
     */
    public Players getRemovedPlayer() {
        return removedPlayer;
    }

    /**
     * Returns the reason why the player was removed.
     */
    public RemovalCause getCause() {
        return cause;
    }
}
