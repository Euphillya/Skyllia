package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.event.players.PlayerPrepareChangeWorldSkyblockEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event triggered when a player changes worlds within the Skyblock environment.
 * <p>
 * This event is called when a player initiates a world change, allowing plugins to perform actions
 * before or after the world transition occurs. It provides details about the player, the portal type
 * used for the transition, the destination location, and whether a safe location check is required.
 * </p>
 * <p>
 * To handle this event, plugins must register an event listener and implement the appropriate handler.
 * </p>
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * import fr.euphyllia.skyllia.api.event.PlayerChangeWorldSkyblockEvent;
 * import org.bukkit.event.EventHandler;
 * import org.bukkit.event.Listener;
 * import org.bukkit.Location;
 * import org.bukkit.entity.Player;
 *
 * public class WorldChangeListener implements Listener {
 *
 *     @EventHandler
 *     public void onPlayerChangeWorld(PlayerChangeWorldSkyblockEvent event) {
 *         Player player = event.getPlayer();
 *         PlayerPrepareChangeWorldSkyblockEvent.PortalType portalType = event.getPortalType();
 *         Location destination = event.getTo();
 *         boolean checkSafety = event.checkSafeLocation();
 *
 *         // Example 1: Logging the world change
 *         player.sendMessage("You are changing worlds via a " + portalType + " portal.");
 *         player.sendMessage("Destination: " + destination.toString());
 *
 *         // Example 2: Modifying the destination location
 *         if (portalType == PlayerPrepareChangeWorldSkyblockEvent.PortalType.NETHER_PORTAL) {
 *             // Redirect to a specific location in the Nether
 *             Location netherLocation = new Location(
 *                 destination.getWorld(),
 *                 destination.getX() + 100,
 *                 destination.getY(),
 *                 destination.getZ() + 100
 *             );
 *             event.setTo(netherLocation);
 *         }
 *
 *         // Example 3: Preventing the world change if the location is unsafe
 *         if (checkSafety && !isSafeLocation(destination)) {
 *             player.sendMessage("The destination location is unsafe. World change canceled.");
 *             // Cancel the event by not proceeding (if the event is cancellable)
 *             // Note: This event is not cancellable in its current implementation.
 *         }
 *     }
 *
 *     /**
 *      * Checks if a given location is safe for the player to teleport.
 *      *
 *      * @param location The location to check.
 *      * @return True if the location is safe, false otherwise.
 *      *\/
 *     private boolean isSafeLocation(Location location) {
 *         // Implement safety checks, such as checking for nearby entities, sufficient space, etc.
 *         return true; // Placeholder implementation
 *     }
 * }
 * }</pre>
 *
 * @see PlayerPrepareChangeWorldSkyblockEvent
 */
public class PlayerChangeWorldSkyblockEvent extends Event {

    /**
     * The handler list for this event.
     */
    private static final HandlerList handlerList = new HandlerList();

    /**
     * The player who is changing worlds.
     */
    private final Player player;

    /**
     * The type of portal used for the world change.
     */
    private final PlayerPrepareChangeWorldSkyblockEvent.PortalType portalType;

    /**
     * Indicates whether the event should check for a safe location.
     */
    private boolean checkSafeLocation = false;

    /**
     * The destination location where the player will be teleported.
     */
    private Location to;

    /**
     * Constructs a new {@code PlayerChangeWorldSkyblockEvent}.
     *
     * @param player           The player changing worlds.
     * @param portalType       The type of portal used for the transition.
     * @param to               The destination location.
     * @param checkSafeLocation Whether to perform a safety check on the destination.
     */
    public PlayerChangeWorldSkyblockEvent(Player player, PlayerPrepareChangeWorldSkyblockEvent.PortalType portalType, Location to, boolean checkSafeLocation) {
        super(false);
        this.player = player;
        this.portalType = portalType;
        this.to = to;
        this.checkSafeLocation = checkSafeLocation;
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
     * Retrieves the type of portal used for the world change.
     *
     * @return The portal type.
     */
    public PlayerPrepareChangeWorldSkyblockEvent.PortalType getPortalType() {
        return this.portalType;
    }

    /**
     * Retrieves the player who is changing worlds.
     *
     * @return The player involved in the world change.
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Checks whether the event should verify if the destination location is safe.
     *
     * @return {@code true} if a safety check is required, {@code false} otherwise.
     */
    public boolean checkSafeLocation() {
        return this.checkSafeLocation;
    }

    /**
     * Retrieves the destination location where the player will be teleported.
     *
     * @return The destination {@link Location}.
     */
    public Location getTo() {
        return this.to;
    }

    /**
     * Sets a new destination location for the player.
     *
     * @param to The new {@link Location} where the player will be teleported.
     */
    public void setTo(Location to) {
        this.to = to;
    }

    /**
     * Sets whether the event should perform a safety check on the destination location.
     *
     * @param checkSafeLocation {@code true} to enable safety checks, {@code false} to disable.
     */
    public void setCheckSafeLocation(boolean checkSafeLocation) {
        this.checkSafeLocation = checkSafeLocation;
    }
}
