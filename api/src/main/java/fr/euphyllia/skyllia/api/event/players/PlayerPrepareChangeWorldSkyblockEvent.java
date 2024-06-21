package fr.euphyllia.skyllia.api.event.players;

import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player touches a portal.
 */
public class PlayerPrepareChangeWorldSkyblockEvent extends Event implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();
    private final Player player;
    private final PortalType portalType;
    private final WorldConfig worldConfig;
    private boolean cancel = false;

    /**
     * Constructs a new PlayerPrepareChangeWorldSkyblockEvent.
     *
     * @param player The player who touched the portal.
     * @param wc The world configuration.
     * @param pt The type of the portal.
     */
    public PlayerPrepareChangeWorldSkyblockEvent(Player player, WorldConfig wc, PortalType pt) {
        this.player = player;
        this.worldConfig = wc;
        this.portalType = pt;
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
     * Gets the type of the portal.
     *
     * @return The portal type.
     */
    public PortalType getPortalType() {
        return this.portalType;
    }

    /**
     * Gets the world configuration.
     *
     * @return The world configuration.
     */
    public WorldConfig getWorldConfig() {
        return this.worldConfig;
    }

    /**
     * Gets the player who touched the portal.
     *
     * @return The player.
     */
    public Player getPlayer() {
        return this.player;
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

    /**
     * Represents the types of portals that can be touched by a player.
     */
    public enum PortalType {
        NETHER, END
    }
}
