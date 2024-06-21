package fr.euphyllia.skyllia.api.event.players;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the player returns to the spawn using the plugin.
 */
public class PlayerTeleportSpawnEvent extends Event implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();
    private final Player bPlayer;
    private Location finalLocation;
    private boolean cancel = false;

    /**
     * Constructs a new PlayerTeleportSpawnEvent.
     *
     * @param player The player who is teleporting to the spawn.
     * @param location The final location where the player will be teleported.
     */
    public PlayerTeleportSpawnEvent(Player player, Location location) {
        this.bPlayer = player;
        this.finalLocation = location;
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
     * Gets the player who is teleporting to the spawn.
     *
     * @return The player.
     */
    public Player getPlayer() {
        return this.bPlayer;
    }

    /**
     * Gets the final location where the player will be teleported.
     *
     * @return The final location.
     */
    public Location getFinalLocation() {
        return finalLocation;
    }

    /**
     * Sets the final location where the player will be teleported.
     *
     * @param finalLocation The new final location.
     */
    public void setFinalLocation(Location finalLocation) {
        this.finalLocation = finalLocation;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
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
