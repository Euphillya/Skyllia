package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SkyblockChangePermissionEvent extends Event {


    private static final HandlerList handlerList = new HandlerList();
    private final Island island;
    private final PermissionsType permissionsType;
    private final RoleType roleType;
    private final long permission;

    public SkyblockChangePermissionEvent(Island island, PermissionsType permissionsType, RoleType roleType, long permissions) {
        super(true);
        this.island = island;
        this.permissionsType = permissionsType;
        this.roleType = roleType;
        this.permission = permissions;
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

    public long getPermission() {
        return this.permission;
    }

    public PermissionsType getPermissionsType() {
        return this.permissionsType;
    }

    public RoleType getRoleType() {
        return this.roleType;
    }
}
