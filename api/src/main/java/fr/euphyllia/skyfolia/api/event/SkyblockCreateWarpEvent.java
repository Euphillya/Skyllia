package fr.euphyllia.skyfolia.api.event;

import fr.euphyllia.skyfolia.api.skyblock.Island;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Appelé quand l'île veut créer un nouveau warp. <br />
 * L'événement n'est pas appelé si le warp se nomme "home". <br />
 * Pourrait être empêché avec un plugin. <br />
 */
public class SkyblockCreateWarpEvent extends Event implements Cancellable {


    private static final HandlerList handlerList = new HandlerList();
    private final Island island;
    private boolean cancel = false;
    private String warp;
    private Location location;


    public SkyblockCreateWarpEvent(Island island, String warpName, Location warpLocation) {
        super(true);
        this.island = island;
        this.warp = warpName;
        this.location = warpLocation;
    }


    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }

    public Island getIsland() {
        return this.island;
    }

    public String getWarpName() {
        return this.warp;
    }

    public Location getWarpLocation() {
        return this.location;
    }

    public void setWarpName(String warp) {
        this.warp = warp;
    }

    public void setWarpLocation(Location location) {
        this.location = location;
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
