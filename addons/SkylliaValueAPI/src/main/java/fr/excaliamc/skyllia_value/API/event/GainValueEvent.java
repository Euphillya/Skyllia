package fr.excaliamc.skyllia_value.API.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GainValueEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();
    private final Island skyblockPlayer;
    private final double islandValue;
    private final double gainValue;

    public GainValueEvent(Island skyblock, double islandValue, double gainValue) {
        super(true);
        this.skyblockPlayer = skyblock;
        this.islandValue = islandValue;
        this.gainValue = gainValue;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public Island getSkyblockPlayer() {
        return this.skyblockPlayer;
    }

    public double getIslandValue() {
        return this.islandValue;
    }

    public double getGainValue() {
        return gainValue;
    }
}
