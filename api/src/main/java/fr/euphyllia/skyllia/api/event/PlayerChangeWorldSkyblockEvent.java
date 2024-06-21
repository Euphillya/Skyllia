package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.event.players.PlayerPrepareChangeWorldSkyblockEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired when a player changes worlds in Skyblock.
 */
public class PlayerChangeWorldSkyblockEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();
    private final Player player;
    private final PlayerPrepareChangeWorldSkyblockEvent.PortalType portalType;
    private boolean checkSafeLocation = false;
    private Location to;

    /**
     * Constructs a new PlayerChangeWorldSkyblockEvent.
     *
     * @param player The player changing worlds.
     * @param pt The type of portal used.
     * @param to The location the player is changing to.
     * @param safeLocation Whether to check for a safe location.
     */
    public PlayerChangeWorldSkyblockEvent(Player player, PlayerPrepareChangeWorldSkyblockEvent.PortalType pt, Location to, boolean safeLocation) {
        super(false);
        this.player = player;
        this.portalType = pt;
        this.to = to;
        this.checkSafeLocation = safeLocation;
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
     * Gets the type of portal used.
     *
     * @return The portal type.
     */
    public PlayerPrepareChangeWorldSkyblockEvent.PortalType getPortalType() {
        return this.portalType;
    }

    /**
     * Gets the player changing worlds.
     *
     * @return The player.
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Checks if the event should check for a safe location.
     *
     * @return True if checking for a safe location, false otherwise.
     */
    public boolean checkSafeLocation() {
        return checkSafeLocation;
    }

    /**
     * Gets the location the player is changing to.
     *
     * @return The destination location.
     */
    public Location getTo() {
        return to;
    }

    /**
     * Sets the location the player is changing to.
     *
     * @param to The new destination location.
     */
    public void setTo(Location to) {
        this.to = to;
    }

    /**
     * Sets whether to check for a safe location.
     *
     * @param safeLoc True to check for a safe location, false otherwise.
     */
    public void setCheckSafeLocation(boolean safeLoc) {
        this.checkSafeLocation = safeLoc;
    }
}
