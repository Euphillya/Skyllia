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

/**
 * Represents a Skyblock island and provides methods to manage its properties and members.
 */
public abstract class Island {

    /**
     * Gets the creation date of the island.
     *
     * @return The creation date.
     */
    public abstract Timestamp getCreateDate();

    /**
     * Gets the unique identifier of the island.
     *
     * @return The island's UUID.
     */
    public abstract UUID getId();

    /**
     * Gets the size of the island.
     *
     * @return The size of the island.
     */
    public abstract double getSize();

    /**
     * Sets the size of the island.
     *
     * @param rayon The new size of the island.
     * @return True if the size was successfully set, false otherwise.
     * @throws MaxIslandSizeExceedException If the new size exceeds the maximum allowed size.
     */
    public abstract boolean setSize(double rayon) throws MaxIslandSizeExceedException;

    /**
     * Gets the list of warps for the island.
     *
     * @return A list of warps.
     */
    public abstract @Nullable CopyOnWriteArrayList<WarpIsland> getWarps();

    /**
     * Gets a warp by its name.
     *
     * @param name The name of the warp.
     * @return The warp with the specified name, or null if not found.
     */
    public abstract @Nullable WarpIsland getWarpByName(String name);

    /**
     * Adds a new warp to the island.
     *
     * @param name The name of the warp.
     * @param loc The location of the warp.
     * @param ignoreEvent Whether to ignore the event.
     * @return True if the warp was successfully added, false otherwise.
     */
    public abstract boolean addWarps(String name, Location loc, boolean ignoreEvent);

    /**
     * Deletes a warp from the island.
     *
     * @param name The name of the warp.
     * @return True if the warp was successfully deleted, false otherwise.
     */
    public abstract boolean delWarp(String name);

    /**
     * Checks if the island is disabled.
     *
     * @return True if the island is disabled, false otherwise.
     */
    public abstract boolean isDisable();

    /**
     * Sets the disabled state of the island.
     *
     * @param disable The new disabled state.
     * @return True if the state was successfully set, false otherwise.
     */
    public abstract boolean setDisable(boolean disable);

    /**
     * Checks if the island is private.
     *
     * @return True if the island is private, false otherwise.
     */
    public abstract boolean isPrivateIsland();

    /**
     * Sets the private state of the island.
     *
     * @param privateIsland The new private state.
     * @return True if the state was successfully set, false otherwise.
     */
    public abstract boolean setPrivateIsland(boolean privateIsland);

    /**
     * Gets the list of members on the island.
     *
     * @return A list of members.
     */
    public abstract CopyOnWriteArrayList<Players> getMembers();

    /**
     * Gets a member by their UUID.
     *
     * @param mojangId The UUID of the member.
     * @return The member with the specified UUID, or null if not found.
     */
    public abstract Players getMember(UUID mojangId);

    /**
     * Gets a member by their name.
     *
     * @param playerName The name of the member.
     * @return The member with the specified name, or null if not found.
     */
    public abstract @Nullable Players getMember(String playerName);

    /**
     * Removes a member from the island.
     *
     * @param players The member to remove.
     * @return True if the member was successfully removed, false otherwise.
     */
    public abstract boolean removeMember(Players players);

    /**
     * Updates a member's information on the island.
     *
     * @param member The member to update.
     * @return True if the member was successfully updated, false otherwise.
     */
    public abstract boolean updateMember(Players member);

    /**
     * Updates a permission for a specific role on the island.
     *
     * @param permissionsType The type of permission to update.
     * @param roleType The role to update the permission for.
     * @param permissions The new permission value.
     * @return True if the permission was successfully updated, false otherwise.
     */
    public abstract boolean updatePermission(PermissionsType permissionsType, RoleType roleType, long permissions);

    /**
     * Gets the position of the island.
     *
     * @return The position of the island.
     */
    public abstract Position getPosition();

    /**
     * Gets the maximum number of members allowed on the island.
     *
     * @return The maximum number of members.
     */
    public abstract int getMaxMembers();

    /**
     * Sets the maximum number of members allowed on the island.
     *
     * @param maxMembers The new maximum number of members.
     * @return True if the maximum number of members was successfully set, false otherwise.
     */
    public abstract boolean setMaxMembers(int maxMembers);

    /**
     * Updates the game rules for the island.
     *
     * @param gamerules The new game rules value.
     * @return True if the game rules were successfully updated, false otherwise.
     */
    public abstract boolean updateGamerule(long gamerules);

    /**
     * Gets the current game rule permission value.
     *
     * @return The game rule permission value.
     */
    public abstract long getGameRulePermission();

    /**
     * Gets the permission value for a specific role and permission type.
     *
     * @param permissionsType The type of permission.
     * @param roleType The role type.
     * @return The permission value.
     */
    public abstract long getPermission(PermissionsType permissionsType, RoleType roleType);
}
