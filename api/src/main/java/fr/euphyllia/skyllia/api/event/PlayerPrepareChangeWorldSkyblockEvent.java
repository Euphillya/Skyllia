package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Appelé quand un joueur touche un portail
 */
public class PlayerPrepareChangeWorldSkyblockEvent extends Event implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();
    private final Player player;
    private final PortalType portalType;
    private final WorldConfig worldConfig;
    private boolean cancel = false;

    public PlayerPrepareChangeWorldSkyblockEvent(Player player, WorldConfig wc, PortalType pt) {
        super(true);
        this.player = player;
        this.worldConfig = wc;
        this.portalType = pt;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }

    public PortalType getPortalType() {
        return this.portalType;
    }

    public WorldConfig getWorldConfig() {
        return this.worldConfig;
    }

    public Player getPlayer() {
        return this.player;
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

    public enum PortalType {
        NETHER, END
    }
}
