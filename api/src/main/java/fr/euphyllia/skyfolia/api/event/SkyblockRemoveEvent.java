package fr.euphyllia.skyfolia.api.event;

import fr.euphyllia.skyfolia.api.skyblock.Island;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SkyblockRemoveEvent extends Event {


    private static final HandlerList handlerList = new HandlerList();
    private final Island island;


    public SkyblockRemoveEvent(Island island) {
        super(true);
        this.island = island;
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
