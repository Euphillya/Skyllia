package fr.euphyllia.skyllia.api.event.players;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Call when the player returns to the spawn using the plugin.
 */
public class PlayerTeleportSpawnEvent extends Event implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();
    private final Player bPlayer;
    private Location finalLocation;
    private boolean cancel = false;

    public PlayerTeleportSpawnEvent(Player player, Location location) {
        this.bPlayer = player;
        this.finalLocation = location;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public Player getPlayer() {
        return this.bPlayer;
    }

    public Location getFinalLocation() {
        return finalLocation;
    }

    public void setFinalLocation(Location finalLocation) {
        this.finalLocation = finalLocation;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }

    /**
     * Gets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins
     *
     * @return true if this event is cancelled
     */
    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    /**
     * Sets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins.
     *
     * @param cancel true if you wish to cancel this event
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
