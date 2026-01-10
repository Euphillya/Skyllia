package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.event.Event;

public abstract class IslandEvent extends Event {

    private final Island island;

    protected IslandEvent(Island island, boolean async) {
        super(async);
        this.island = island;
    }

    public Island getIsland() {
        return island;
    }
}