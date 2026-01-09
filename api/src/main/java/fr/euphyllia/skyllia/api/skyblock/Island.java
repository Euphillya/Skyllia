package fr.euphyllia.skyllia.api.skyblock;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.exceptions.MaxIslandSizeExceedException;
import fr.euphyllia.skyllia.api.permissions.CompiledPermissions;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * Represents a Skyblock island and provides methods to manage its properties and members.
 */
public abstract class Island {

    private static final Logger log = LoggerFactory.getLogger(Island.class);
    private transient volatile CompiledPermissions compiledPermissions;

    /**
     * Gets the owner of the island.
     *
     * @return The {@link Players} object representing the island owner.
     */
    public abstract Players getOwner();

    /**
     * Gets the creation date of the island.
     *
     * @return The creation date as a {@link Timestamp}, or {@code null} if unknown.
     */
    public abstract Timestamp getCreateDate();

    /**
     * Gets the unique identifier of the island.
     *
     * @return The island's UUID.
     */
    public abstract UUID getId();

    /**
     * Gets the size (radius) of the island.
     *
     * @return The island size as a {@code double}.
     */
    public abstract double getSize();

    /**
     * Sets the island size (radius).
     *
     * @param rayon The new size of the island.
     * @return {@code true} if the size was successfully set; {@code false} otherwise.
     * @throws MaxIslandSizeExceedException If the new size exceeds the maximum allowed.
     */
    public abstract boolean setSize(double rayon) throws MaxIslandSizeExceedException;

    /**
     * Gets a list of warps for the island.
     *
     * @return A list of {@link WarpIsland} objects, or {@code null} if none.
     */
    public abstract @Nullable List<WarpIsland> getWarps();

    /**
     * Gets a warp by its name.
     *
     * @param name The warp name.
     * @return The {@link WarpIsland}, or {@code null} if not found.
     */
    public abstract @Nullable WarpIsland getWarpByName(String name);

    /**
     * Adds a warp to the island.
     *
     * @param name        The name of the warp.
     * @param loc         The {@link Location} of the warp.
     * @param ignoreEvent If {@code true}, the warp creation event is not called.
     * @return {@code true} if successfully added, {@code false} otherwise.
     */
    public abstract boolean addWarps(String name, Location loc, boolean ignoreEvent);

    /**
     * Deletes a warp from the island.
     *
     * @param name The name of the warp to delete.
     * @return {@code true} if successfully deleted, {@code false} otherwise.
     */
    public abstract boolean delWarp(String name);

    /**
     * Checks if the island is disabled.
     *
     * @return {@code true} if disabled, {@code false} otherwise.
     */
    public abstract boolean isDisable();

    /**
     * Sets the disabled state of the island.
     *
     * @param disable {@code true} to disable, {@code false} to enable.
     * @return {@code true} if successfully updated, {@code false} otherwise.
     */
    public abstract boolean setDisable(boolean disable);

    /**
     * Checks if the island is private.
     *
     * @return {@code true} if private, {@code false} otherwise.
     */
    public abstract boolean isPrivateIsland();

    /**
     * Sets the island's privacy state.
     *
     * @param privateIsland {@code true} to make it private, {@code false} otherwise.
     * @return {@code true} if successfully updated, {@code false} otherwise.
     */
    public abstract boolean setPrivateIsland(boolean privateIsland);

    /**
     * Gets the list of all members in the island (from the database).
     *
     * @return A {@link List} of {@link Players}.
     */
    public abstract List<Players> getMembers();

    /**
     * Gets the list of all banned members in the island.
     *
     * @return A {@link List} of {@link Players} who are banned.
     */
    public abstract List<Players> getBannedMembers();

    /**
     * Gets the cached list of all members on the island (from memory/cache).
     *
     * @return A {@link List} of {@link Players}.
     */
    public abstract List<Players> getMembersCached();

    /**
     * Gets a specific member by their UUID.
     *
     * @param mojangId The UUID of the member.
     * @return The matching {@link Players}, or {@code null} if not found.
     */
    public abstract Players getMember(UUID mojangId);

    /**
     * Gets a specific member by their name.
     *
     * @param playerName The name of the player.
     * @return The matching {@link Players}, or {@code null} if not found.
     */
    public abstract @Nullable Players getMember(String playerName);

    /**
     * Removes a member from the island.
     *
     * @param players The {@link Players} to remove.
     * @return {@code true} if successfully removed, {@code false} otherwise.
     */
    public abstract boolean removeMember(Players players);

    /**
     * Updates a member's information on the island.
     *
     * @param member The {@link Players} object containing new data.
     * @return {@code true} if successfully updated, {@code false} otherwise.
     */
    public abstract boolean updateMember(Players member);

    /**
     * Gets the position (region-based) of the island.
     *
     * @return A {@link Position} object representing the island's coordinates.
     */
    public abstract Position getPosition();

    /**
     * Gets the maximum number of members allowed on the island.
     *
     * @return The max member count.
     */
    public abstract int getMaxMembers();

    /**
     * Sets the maximum number of members allowed on the island.
     *
     * @param maxMembers The new maximum.
     * @return {@code true} if successfully updated, {@code false} otherwise.
     */
    public abstract boolean setMaxMembers(int maxMembers);

    public final CompiledPermissions getCompiledPermissions() {
        CompiledPermissions local = this.compiledPermissions;
        if (local == null) {
            local = new CompiledPermissions(SkylliaAPI.getPermissionRegistry()); // Création de permissions compilées
            log.info("Rechargement des permissions compilées pour l'île " + getId());
            this.compiledPermissions = local;
        }
        return local;
    }
}
