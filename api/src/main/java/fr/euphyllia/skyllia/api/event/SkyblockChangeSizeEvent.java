package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SkyblockChangeSizeEvent extends Event {


    private static final HandlerList handlerList = new HandlerList();
    private final Island island;
    private final double sizeIsland;

    public SkyblockChangeSizeEvent(Island island, double rayon) {
        super(true);
        this.island = island;
        this.sizeIsland = rayon;
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

    public double getSizeIsland() {
        return this.sizeIsland;
    }
}

