package fr.euphyllia.skyllia.api.event.players;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event triggered when a player is about to teleport to the spawn using the plugin.
 * <p>
 * This event is called synchronously when a player initiates a teleport to the spawn point
 * via the plugin. It allows other plugins to perform actions before or after the teleport occurs,
 * such as validating the teleport, modifying the final location, or enforcing restrictions.
 * Additionally, this event can be cancelled by other plugins to prevent the teleport from taking place.
 * </p>
 * <p>
 * Note: This event is triggered exclusively through the plugin's teleport functionality and does not
 * respond to other forms of teleportation (e.g., commands or other plugins).
 * </p>
 * <p>
 * To handle this event, plugins must register an event listener and implement the appropriate handler.
 * </p>
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * import fr.euphyllia.skyllia.api.event.players.PlayerTeleportSpawnEvent;
 * import org.bukkit.Location;
 * import org.bukkit.entity.Player;
 * import org.bukkit.event.EventHandler;
 * import org.bukkit.event.Listener;
 *
 * public class TeleportSpawnListener implements Listener {
 *
 *     @EventHandler
 *     public void onPlayerTeleportSpawn(PlayerTeleportSpawnEvent event) {
 *         Player player = event.getPlayer();
 *         Location destination = event.getFinalLocation();
 *
 *         // Example 1: Logging the teleport attempt
 *         player.sendMessage("Preparing to teleport to spawn...");
 *         System.out.println("Player " + player.getName() + " is teleporting to spawn at location " + destination + ".");
 *
 *         // Example 2: Modifying the final teleport location
 *         if (player.hasPermission("skyblock.teleport.vip")) {
 *             Location vipLocation = destination.clone().add(10, 0, 10); // Offset for VIP players
 *             event.setFinalLocation(vipLocation);
 *             player.sendMessage("VIP teleportation: Your spawn location has been adjusted.");
 *             System.out.println("Player " + player.getName() + " teleport location adjusted for VIP.");
 *         }
 *
 *         // Example 3: Preventing the teleport under certain conditions
 *         if (player.getHealth() < 5.0) {
 *             event.setCancelled(true);
 *             player.sendMessage("Teleport to spawn cancelled due to low health.");
 *             System.out.println("Teleportation for player " + player.getName() + " cancelled due to low health.");
 *             return;
 *         }
 *     }
 * }
 * }</pre>
 *
 * @see Player
 * @see Location
 */
public class PlayerTeleportSpawnEvent extends Event implements Cancellable {

    /**
     * The handler list for this event.
     */
    private static final HandlerList handlerList = new HandlerList();

    /**
     * The player who is teleporting to the spawn.
     */
    private final Player bPlayer;

    /**
     * The final location where the player will be teleported.
     */
    private Location finalLocation;

    /**
     * Indicates whether this event has been cancelled.
     */
    private boolean cancel = false;

    /**
     * Constructs a new {@code PlayerTeleportSpawnEvent}.
     *
     * @param player   The player who is teleporting to the spawn.
     * @param location The final location where the player will be teleported.
     */
    public PlayerTeleportSpawnEvent(Player player, Location location) {
        super(false);
        this.bPlayer = player;
        this.finalLocation = location;
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
     * Retrieves the player who is teleporting to the spawn.
     *
     * @return The player involved in the teleportation.
     */
    public Player getPlayer() {
        return this.bPlayer;
    }

    /**
     * Retrieves the final location where the player will be teleported.
     *
     * @return The final {@link Location} of the teleportation.
     */
    public Location getFinalLocation() {
        return this.finalLocation;
    }

    /**
     * Sets a new final location for the player to be teleported.
     *
     * @param finalLocation The new {@link Location} where the player will be teleported.
     */
    public void setFinalLocation(Location finalLocation) {
        this.finalLocation = finalLocation;
    }

    /**
     * Checks whether this event has been cancelled. A cancelled event will prevent the teleport
     * from occurring, but the event will still pass to other plugins.
     *
     * @return {@code true} if this event is cancelled, {@code false} otherwise.
     */
    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    /**
     * Sets the cancellation state of this event. A cancelled event will prevent the teleport
     * from occurring, but the event will still pass to other plugins.
     *
     * @param cancel {@code true} to cancel this event, {@code false} to allow it to proceed.
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
