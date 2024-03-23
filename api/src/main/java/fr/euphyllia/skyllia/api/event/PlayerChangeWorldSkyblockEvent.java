package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.event.players.PlayerPrepareChangeWorldSkyblockEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerChangeWorldSkyblockEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();
    private final Player player;
    private final PlayerPrepareChangeWorldSkyblockEvent.PortalType portalType;
    private boolean checkSafeLocation = false;
    private Location to;

    public PlayerChangeWorldSkyblockEvent(Player player, PlayerPrepareChangeWorldSkyblockEvent.PortalType pt, Location to, boolean safeLocation) {
        super(false);
        this.player = player;
        this.portalType = pt;
        this.to = to;
        this.checkSafeLocation = safeLocation;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }

    public PlayerPrepareChangeWorldSkyblockEvent.PortalType getPortalType() {
        return this.portalType;
    }

    public Player getPlayer() {
        return this.player;
    }

    public boolean checkSafeLocation() {
        return checkSafeLocation;
    }

    public Location getTo() {
        return to;
    }

    public void setTo(Location to) {
        this.to = to;
    }

    public void setCheckSafeLocation(boolean safeLoc) {
        this.checkSafeLocation = safeLoc;
    }
}