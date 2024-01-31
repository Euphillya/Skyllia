package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SkyblockChangeGameRuleEvent  extends Event {


    private static final HandlerList handlerList = new HandlerList();
    private final Island island;
    private final long gamerule;

    public SkyblockChangeGameRuleEvent(Island island, long gamerules) {
        super(true);
        this.island = island;
        this.gamerule = gamerules;
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

    public long getGamerule() {
        return this.gamerule;
    }
}
