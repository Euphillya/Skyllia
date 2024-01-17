package fr.euphyllia.skyfolia.api.event;

import fr.euphyllia.skyfolia.api.skyblock.Island;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Appelé quand l'île va supprimer un warp. <br />
 * Pourrait être empêché avec un plugin. <br />
 */
public class SkyblockDeleteWarpEvent extends Event implements Cancellable {


    private static final HandlerList handlerList = new HandlerList();
    private final Island island;
    private final String warp;
    private boolean cancel = false;


    public SkyblockDeleteWarpEvent(Island island, String warpName) {
        super(true);
        this.island = island;
        this.warp = warpName;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public String getWarpName() {
        return warp;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }

    public Island getIsland() {
        return this.island;
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
