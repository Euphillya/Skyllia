package fr.euphyllia.skyllia.api.skyblock;

import fr.euphyllia.skyllia.api.exceptions.MaxIslandSizeExceedException;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Island {

    public abstract Timestamp getCreateDate();

    public abstract UUID getId();

    public abstract double getSize();

    public abstract boolean setSize(double rayon) throws MaxIslandSizeExceedException;

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

    @Deprecated(forRemoval = true)
    public abstract boolean updatePermissionIsland(PermissionsType permissionsType, RoleType roleType, long permissions);

    public abstract boolean updatePermission(PermissionsType permissionsType, RoleType roleType, long permissions);

    public abstract Position getPosition();

    public abstract int getMaxMembers();

    public abstract boolean setMaxMembers(int maxMembers);

    public abstract boolean updateGamerule(long gamerules);

    public abstract long getGameRulePermission();

    public abstract long getPermission(PermissionsType permissionsType, RoleType roleType);
}
