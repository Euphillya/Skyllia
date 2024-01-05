package fr.euphyllia.skyfolia.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SkyblockChangeEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();

    public SkyblockChangeEvent() {
        super(true);
    }


    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }
}
