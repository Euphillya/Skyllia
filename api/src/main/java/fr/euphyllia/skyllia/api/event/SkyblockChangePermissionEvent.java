package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event triggered when permissions are changed on a Skyblock island for a specific role.
 * <p>
 * This event is called when a plugin modifies the permissions associated with a particular role
 * on a Skyblock island. It allows other plugins to react to permission changes, such as logging the change,
 * enforcing additional rules, or modifying the permission values.
 * </p>
 * <p>
 * To handle this event, plugins must register an event listener and implement the appropriate handler.
 * </p>
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * import fr.euphyllia.skyllia.api.event.SkyblockChangePermissionEvent;
 * import fr.euphyllia.skyllia.api.skyblock.Island;
 * import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
 * import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
 * import org.bukkit.event.EventHandler;
 * import org.bukkit.event.Listener;
 *
 * public class PermissionChangeListener implements Listener {
 *
 *     @EventHandler
 *     public void onPermissionChange(SkyblockChangePermissionEvent event) {
 *         Island island = event.getIsland();
 *         PermissionsType type = event.getPermissionsType();
 *         RoleType role = event.getRoleType();
 *         long newPermission = event.getPermission();
 *
 *         // Example 1: Logging the permission change
 *         System.out.println("Permissions of " + type + " for role " + role + " on island " + island.getName() + " have been updated to: " + newPermission);
 *
 *         // Example 2: Enforcing additional rules after a permission change
 *         if (type == PermissionsType.BUILD && role == RoleType.MEMBER) {
 *             if ((newPermission & PermissionsType.BUILD.getFlag()) == 0) {
 *                 // Remove existing blocks if the member can no longer build
 *                 island.removeBlocksForRole(role);
 *             }
 *         }
 *
 *         // Example 3: Modifying the permission value (if the event is designed to allow it)
 *         // Note: This event does not currently support modifying the permission value.
 *     }
 * }
 * }</pre>
 *
 * @see Island
 * @see RoleType
 * @see PermissionsType
 */
public class SkyblockChangePermissionEvent extends Event {

    /**
     * The handler list for this event.
     */
    private static final HandlerList handlerList = new HandlerList();

    /**
     * The island where the permissions are being changed.
     */
    private final Island island;

    /**
     * The type of permissions being changed.
     */
    private final PermissionsType permissionsType;

    /**
     * The role type affected by the permission change.
     */
    private final RoleType roleType;

    /**
     * The new value of the permissions.
     */
    private final long permission;

    /**
     * Constructs a new {@code SkyblockChangePermissionEvent}.
     *
     * @param island          The island where the permissions are being changed.
     * @param permissionsType The type of permissions being changed.
     * @param roleType        The role type affected by the permission change.
     * @param permissions     The new value of the permissions.
     */
    public SkyblockChangePermissionEvent(Island island, PermissionsType permissionsType, RoleType roleType, long permissions) {
        super(true);
        this.island = island;
        this.permissionsType = permissionsType;
        this.roleType = roleType;
        this.permission = permissions;
    }

    /**
     * Retrieves the handler list for this event.
     *
     * @return The handler list.
     */
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    /**
     * Retrieves the handlers associated with this event.
     *
     * @return The handler list.
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }

    /**
     * Retrieves the island where the permissions are being changed.
     *
     * @return The island.
     */
    public Island getIsland() {
        return this.island;
    }

    /**
     * Retrieves the new value of the permissions.
     *
     * @return The new permission value.
     */
    public long getPermission() {
        return this.permission;
    }

    /**
     * Retrieves the type of permissions being changed.
     *
     * @return The permissions type.
     */
    public PermissionsType getPermissionsType() {
        return this.permissionsType;
    }

    /**
     * Retrieves the role type affected by the permission change.
     *
     * @return The role type.
     */
    public RoleType getRoleType() {
        return this.roleType;
    }
}
