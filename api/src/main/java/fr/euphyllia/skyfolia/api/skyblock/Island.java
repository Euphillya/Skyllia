package fr.euphyllia.skyfolia.api.skyblock;

import fr.euphyllia.skyfolia.api.exceptions.MaxIslandSizeExceedException;
import fr.euphyllia.skyfolia.api.skyblock.model.IslandType;
import fr.euphyllia.skyfolia.api.skyblock.model.Position;
import fr.euphyllia.skyfolia.api.skyblock.model.WarpIsland;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Island {

    public abstract Timestamp getCreateDate();

    public abstract UUID getId();

    public abstract double getSize();

    public abstract void setSize(int rayon) throws MaxIslandSizeExceedException;

    public abstract @Nullable CopyOnWriteArrayList<WarpIsland> getWarps();

    public abstract @Nullable WarpIsland getWarpByName(String name);

    public abstract boolean addWarps(String name, Location loc, boolean ignoreEvent);

    public abstract boolean delWarp(String name);

    public abstract boolean isDisable();

    public abstract boolean setDisable(boolean disable);

    public abstract boolean isPrivateIsland();

    public abstract boolean setPrivateIsland(boolean privateIsland);

    public abstract CopyOnWriteArrayList<Players> getMembers();

    public abstract Players getMember(UUID mojangId);

    public abstract @Nullable Players getMember(String playerName);

    public abstract boolean removeMember(Players players);

    public abstract boolean updateMember(Players member);

    public abstract UUID getOwnerId();

    public abstract void setOwnerId(UUID ownerId);

    public abstract Position getPosition();

    public abstract IslandType getIslandType();

    public abstract void setIslandType(IslandType islandType);
}
