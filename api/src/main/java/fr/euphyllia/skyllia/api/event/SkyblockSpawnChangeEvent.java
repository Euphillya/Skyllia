package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SkyblockSpawnChangeEvent extends IslandEvent implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();

    private Location spawnLocation;
    private boolean cancel = false;

    public SkyblockSpawnChangeEvent(@NotNull Island island, @NotNull Location spawnLocation) {
        super(island, true);
        this.spawnLocation = spawnLocation;
    }

    /**
     * @return The handler list for this event.
     */
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    /**
     * @return The new spawn location (possibly modified by a previous listener).
     */
    public @NotNull Location getSpawnLocation() {
        return spawnLocation;
    }

    /**
     * Overrides the spawn location that will be persisted.
     *
     * @param spawnLocation The location to use instead.
     */
    public void setSpawnLocation(@NotNull Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
}
