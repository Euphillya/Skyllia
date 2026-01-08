package fr.euphyllia.skyllia.api.event.teleport;

import fr.euphyllia.skyllia.api.event.IslandEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;

public class PlayerTeleportIslandEvent extends IslandEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Location from;
    private final Location to;

    public PlayerTeleportIslandEvent(
            Player player,
            Location from,
            Location to,
            Island island,
            boolean async
    ) {
        super(island, async);
        this.player = player;
        this.from = from;
        this.to = to;
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

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
