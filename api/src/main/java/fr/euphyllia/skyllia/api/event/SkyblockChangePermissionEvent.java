package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is called when permissions are changed on a Skyblock island for a specific role.
 */
public class SkyblockChangePermissionEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();
    private final Island island;
    private final PermissionsType permissionsType;
    private final RoleType roleType;
    private final long permission;

    /**
     * Constructs a new SkyblockChangePermissionEvent.
     *
     * @param island The island where the permissions are being changed.
     * @param permissionsType The type of permissions being changed.
     * @param roleType The role type affected by the permission change.
     * @param permissions The new value of the permissions.
     */
    public SkyblockChangePermissionEvent(Island island, PermissionsType permissionsType, RoleType roleType, long permissions) {
        super(true);
        this.island = island;
        this.permissionsType = permissionsType;
        this.roleType = roleType;
        this.permission = permissions;
    }

    /**
     * Gets the handler list for this event.
     *
     * @return The handler list.
     */
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    /**
     * Gets the handlers for this event.
     *
     * @return The handlers.
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }

    /**
     * Gets the island where the permissions are being changed.
     *
     * @return The island.
     */
    public Island getIsland() {
        return this.island;
    }

    /**
     * Gets the new value of the permissions.
     *
     * @return The new permission value.
     */
    public long getPermission() {
        return this.permission;
    }

    /**
     * Gets the type of permissions being changed.
     *
     * @return The permissions type.
     */
    public PermissionsType getPermissionsType() {
        return this.permissionsType;
    }

    /**
     * Gets the role type affected by the permission change.
     *
     * @return The role type.
     */
    public RoleType getRoleType() {
        return this.roleType;
    }
}
