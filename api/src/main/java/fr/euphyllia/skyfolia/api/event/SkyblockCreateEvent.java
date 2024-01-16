package fr.euphyllia.skyfolia.api.event;

import fr.euphyllia.skyfolia.api.skyblock.Island;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Appelé quand l'île est créée. <br />
 */
public class SkyblockCreateEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();
    private final Island island;

    public SkyblockCreateEvent(Island islandCreate) {
        super(true);
        this.island = islandCreate;
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
}
