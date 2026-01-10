package fr.euphyllia.skyllia.api.event.teleport;

import fr.euphyllia.skyllia.api.event.IslandEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jspecify.annotations.NonNull;

public class PlayerTeleportIslandEvent extends IslandEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Location from;
    private final Location to;
    private final PlayerTeleportEvent.TeleportCause teleportCause;
    private boolean cancelled = false;

    public PlayerTeleportIslandEvent(
            Player player,
            Location from,
            Location to,
            Island island,
            PlayerTeleportEvent.TeleportCause teleportCause,
            boolean cancelled,
            boolean async
    ) {
        super(island, async);
        this.player = player;
        this.from = from;
        this.to = to;
        this.teleportCause = teleportCause;
        this.cancelled = cancelled;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getFrom() {
        return from;
    }

    public Location getTo() {
        return to;
    }

    public PlayerTeleportEvent.TeleportCause getTeleportCause() {
        return teleportCause;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
