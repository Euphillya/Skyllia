package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Appelé quand l'île est créée. <br />
 */
public class SkyblockCreateEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();
    private final Island island;
    private final UUID owner;

    public SkyblockCreateEvent(Island islandCreate, UUID owner) {
        super(true);
        this.island = islandCreate;
        this.owner = owner;
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

    public UUID getOwnerId() {
        return this.owner;
    }
}
