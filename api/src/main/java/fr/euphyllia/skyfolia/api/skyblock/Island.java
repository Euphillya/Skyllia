package fr.euphyllia.skyfolia.api.skyblock;

import fr.euphyllia.skyfolia.api.exceptions.MaxIslandSizeExceedException;
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

    public abstract int getSize();

    public abstract void setSize(int rayon) throws MaxIslandSizeExceedException;

    public abstract @Nullable CopyOnWriteArrayList<WarpIsland> getWarps();

    public abstract @Nullable WarpIsland getWarpByName(String name);

    public abstract boolean addWarps(String name, Location loc);

    public abstract boolean isDisable();

    public abstract void setDisable(boolean disable);

    public abstract boolean isPrivateIsland();

    public abstract void setPrivateIsland(boolean privateIsland);

    public abstract CopyOnWriteArrayList<Players> getMembers();

    public abstract Players getMember(UUID mojangId);

    public abstract Players getMember(String playerName);

    public abstract void removeMember(Players players);

    public abstract boolean updateMember(Players member);

    public abstract UUID getOwnerId();

    public abstract void setOwnerId(UUID ownerId);

    public abstract Position getPosition();

    public abstract String getIslandType();

    public abstract void setIslandType(String islandType);
}
