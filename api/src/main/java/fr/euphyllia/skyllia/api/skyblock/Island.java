package fr.euphyllia.skyllia.api.skyblock;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.coordinate.RegionCoordinate;
import fr.euphyllia.skyllia.api.event.SkyblockSpawnChangeEvent;
import fr.euphyllia.skyllia.api.exceptions.MaxIslandSizeExceedException;
import fr.euphyllia.skyllia.api.permissions.CompiledPermissions;
import fr.euphyllia.skyllia.api.permissions.IslandFlags;
import fr.euphyllia.skyllia.api.skyblock.enums.RemovalCause;
import fr.euphyllia.skyllia.api.skyblock.model.HeightType;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
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

    /**
     * The reserved warp name under which the island spawn point is stored.
     * <p>
     * This name is reserved by Skyllia: it cannot be set or deleted through the
     * regular {@code /is setwarp} / {@code /is delwarp} commands, and is never
     * listed as a public warp.
     * </p>
     */
    public static final String SPAWN_WARP_NAME = "spawn";
    private static final Logger log = LoggerFactory.getLogger(Island.class);

    /**
     * Gets the name of the island.
     *
     * @return The island name, or {@code null} if none has been set.
     */
    public @Nullable
    abstract String getName();

    /**
     * Sets the display name of the island.
     *
     * @param name The new name, or {@code null} to remove the current name.
     * @return {@code true} if successfully updated, {@code false} otherwise.
     */
    public abstract boolean setName(@Nullable String name);

    /**
     * Gets the description of the island.
     *
     * @return The description, or {@code null} if none has been set.
     */
    public abstract @Nullable String getDescription();

    /**
     * Sets the description of the island.
     *
     * @param description The new description, or {@code null} to remove it.
     * @return {@code true} if successfully updated, {@code false} otherwise.
     */
    public abstract boolean setDescription(@Nullable String description);

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
     * Gets the visit warp of the island.
     * <p>
     * Returns the warp named {@code "visit"} if it has been explicitly set via
     * {@code /is setvisit}, or falls back to the {@code "home"} warp if no visit
     * point has been defined. Returns {@code null} if neither exists.
     * </p>
     *
     * @return The visit {@link WarpIsland}, or {@code null} if none is set.
     */
    public @Nullable WarpIsland getVisit() {
        WarpIsland visit = getWarpByName("visit");
        if (visit != null) return visit;
        return getWarpByName("home");
    }

    /**
     * Sets the visit warp of the island to the given location.
     * <p>
     * This is a convenience method equivalent to calling
     * {@code addWarps("visit", location, false)}.
     * </p>
     *
     * @param location The {@link Location} to use as the visit point.
     * @return {@code true} if successfully saved, {@code false} otherwise.
     */
    public boolean setVisit(Location location) {
        return addWarps("visit", location, false);
    }

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
     * Gets the list of all members on the island (from the database).
     *
     * @return A {@link List} of {@link Players}.
     */
    public abstract List<Players> getMembers();

    /**
     * Gets the list of all banned members on the island.
     *
     * @return A {@link List} of {@link Players} who are banned.
     */
    public abstract List<Players> getBannedMembers();

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
     * Removes a member from the island with {@link RemovalCause#KICKED} as the default cause.
     *
     * @param players The member to remove.
     * @return {@code true} if successfully removed, {@code false} otherwise.
     * @deprecated Use {@link #removeMember(Players, RemovalCause)} to provide an explicit cause.
     */
    @Deprecated(since = "3.x")
    public boolean removeMember(Players players) {
        return removeMember(players, RemovalCause.KICKED);
    }

    /**
     * Removes a member from the island with an explicit removal cause.
     *
     * @param players The member to remove.
     * @param cause   The reason for the removal (KICKED, LEAVE, ISLAND_DELETED…).
     * @return {@code true} if successfully removed, {@code false} otherwise.
     */
    public abstract boolean removeMember(Players players, RemovalCause cause);

    /**
     * Updates a member's information on the island.
     *
     * @param member The {@link Players} object containing new data.
     * @return {@code true} if successfully updated, {@code false} otherwise.
     */
    public abstract boolean updateMember(Players member);

    /**
     * Gets the region-based coordinate of the island.
     *
     * @return A {@link RegionCoordinate} representing the island's region coordinates.
     * @since 3.x
     */
    public abstract RegionCoordinate getRegionCoordinate();

    /**
     * Gets the region coordinate of the island.
     * <p>
     * This method is deprecated because {@link Position} was historically
     * used for multiple coordinate types (regions, chunks, etc.), making
     * its purpose ambiguous.
     * </p>
     * <p>
     * Use {@link #getRegionCoordinate()} instead.
     * </p>
     *
     * @return The island's region coordinate.
     * @deprecated since 3.x, replaced by {@link #getRegionCoordinate()}.
     */
    @Deprecated(forRemoval = true, since = "3.x")
    @ApiStatus.ScheduledForRemoval(inVersion = "4.x")
    public Position getPosition() {
        RegionCoordinate coordinate = getRegionCoordinate();
        return new Position(coordinate.x(), coordinate.z());
    }

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

    /**
     * Returns the compiled permission set for this island.
     * <p>
     * Compiled permissions aggregate all role-based and member-specific
     * permission rules into a single, ready-to-query object. The result
     * may be cached; call {@link #invalidateCompiledPermissions()} to
     * force a reload from the database.
     * </p>
     *
     * @return The {@link CompiledPermissions} for this island, never {@code null}.
     */
    public abstract CompiledPermissions getCompiledPermissions();

    /**
     * Invalidates the cached compiled permissions for this island,
     * forcing a reload from the database on the next call to
     * {@link #getCompiledPermissions()}.
     */
    public abstract void invalidateCompiledPermissions();

    /**
     * Returns the compiled flag set for this island in the specified world.
     * <p>
     * If no flags are stored yet for that world, an empty {@link IslandFlags} is returned.
     * </p>
     *
     * @param worldName The name of the world.
     * @return The {@link IslandFlags} for the given world, never {@code null}.
     */
    public abstract IslandFlags getIslandFlags(String worldName);

    /**
     * Invalidates the cached flags for the specified world,
     * forcing a reload from the database on the next call to {@link #getIslandFlags(String)}.
     *
     * @param worldName The name of the world.
     */
    public abstract void invalidateIslandFlags(String worldName);

    /**
     * @deprecated Use {@link #getIslandFlags(String)} instead.
     */
    @Deprecated(forRemoval = true, since = "3.x")
    @ApiStatus.ScheduledForRemoval(inVersion = "4.x")
    public IslandFlags getIslandFlags() {
        List<WorldConfig> worlds = SkylliaAPI.getRegisteredWorlds();
        if (worlds.isEmpty()) return new IslandFlags(SkylliaAPI.getFlagRegistry());
        return getIslandFlags(worlds.getFirst().getWorldName());
    }

    /**
     * @deprecated Use {@link #invalidateIslandFlags(String)} instead.
     */
    @Deprecated(forRemoval = true, since = "3.x")
    @ApiStatus.ScheduledForRemoval(inVersion = "4.x")
    public void invalidateIslandFlags() {
        for (WorldConfig w : SkylliaAPI.getRegisteredWorlds()) {
            invalidateIslandFlags(w.getWorldName());
        }
    }

    /**
     * Gets the center {@link Location} of the island in the specified world.
     * <p>
     * If no center has been stored for this world yet, a fallback location is computed
     * from the island's region position at Y=64, persisted to the database, and returned.
     * </p>
     *
     * @param world The {@link World} for which to retrieve the center location.
     * @return The center {@link Location} of the island in the given world, never {@code null}.
     */
    public abstract Location getCenterLocation(World world);

    /**
     * Sets the center {@link Location} of the island for the world contained in the given location.
     * <p>
     * This updates both the in-memory cache and the database. If multiple worlds are configured
     * for this island, each world has its own center location stored independently.
     * </p>
     *
     * @param location The {@link Location} to store as the island center.
     *                 The world is inferred from {@link Location#getWorld()}.
     */
    public abstract void setCenterLocation(Location location);

    /**
     * Returns the raw, stored custom spawn of this island, if any.
     * <p>
     * Returns {@code null} when no custom spawn has been set; in that case callers
     * should fall back to {@link #getCenterLocation(World)} (see
     * {@link #getSpawnLocation(World)}).
     * </p>
     *
     * @return The custom spawn {@link WarpIsland}, or {@code null} if none is set.
     */
    public @Nullable WarpIsland getSpawn() {
        return getWarpByName(SPAWN_WARP_NAME);
    }

    /**
     * Returns whether this island has an explicit, customized spawn point.
     *
     * @return {@code true} if a custom spawn has been set, {@code false} if the
     * island still uses the default (center-based) spawn.
     */
    public boolean hasCustomSpawn() {
        WarpIsland spawn = getSpawn();
        return spawn != null && spawn.location() != null && spawn.location().getWorld() != null;
    }

    /**
     * Resolves the spawn {@link Location} of this island for the given world.
     * <p>
     * Resolution order:
     * <ol>
     *     <li>The custom spawn set via {@link #setSpawnLocation(Location)} or
     *     {@code /is setspawn}, if it exists and belongs to {@code world}.</li>
     *     <li>Otherwise, the island {@link #getCenterLocation(World) center} of
     *     {@code world}, which acts as the always-available default spawn.</li>
     * </ol>
     * The returned location is a defensive copy and is never {@code null} as long
     * as the world is valid. Callers that want the player to stand on top of the
     * spawn block should add a {@code +0.5} Y offset themselves, mirroring the
     * behaviour of {@code /is home}.
     * </p>
     *
     * @param world The {@link World} for which to resolve the spawn.
     * @return The resolved spawn {@link Location} for the given world.
     */
    public Location getSpawnLocation(World world) {
        WarpIsland spawn = getSpawn();
        if (spawn != null && spawn.location() != null) {
            Location loc = spawn.location();
            if (loc.getWorld() != null && loc.getWorld().equals(world)) {
                return loc.clone();
            }
        }
        return getCenterLocation(world);
    }

    /**
     * Sets a custom spawn point for this island.
     * <p>
     * The spawn is persisted under the reserved {@link #SPAWN_WARP_NAME} warp and
     * becomes the location used by {@code /is home}, respawn, and post-creation
     * teleport. An {@link fr.euphyllia.skyllia.api.event.SkyblockSpawnChangeEvent}
     * is fired beforehand, allowing other addons to adjust or veto the change.
     * </p>
     *
     * @param location The {@link Location} to use as the island spawn. The world
     *                 is inferred from {@link Location#getWorld()}.
     * @return {@code true} if the spawn was successfully saved, {@code false} if
     * the change was cancelled or persistence failed.
     */
    public boolean setSpawnLocation(Location location) {
        SkyblockSpawnChangeEvent event = new SkyblockSpawnChangeEvent(this, location);
        event.callEvent();
        if (event.isCancelled()) {
            return false;
        }
        return addWarps(SPAWN_WARP_NAME, event.getSpawnLocation(), true);
    }

    /**
     * Clears any custom spawn, reverting this island to the default
     * (center-based) spawn point.
     *
     * @return {@code true} if a custom spawn was removed, {@code false} otherwise.
     */
    public boolean resetSpawn() {
        return delWarp(SPAWN_WARP_NAME);
    }

    /**
     * Returns the custom minimum build height for this island in the given world,
     * or {@code null} if no custom value is set (the world default applies).
     *
     * @param worldName The name of the world.
     * @return The custom min height, or {@code null}.
     */
    public abstract @Nullable Integer getBuildMinHeight(String worldName);

    /**
     * Returns the custom maximum build height for this island in the given world,
     * or {@code null} if no custom value is set (the world default applies).
     *
     * @param worldName The name of the world.
     * @return The custom max height, or {@code null}.
     */
    public abstract @Nullable Integer getBuildMaxHeight(String worldName);

    /**
     * Sets a custom build-height limit (min or max) for this island in the given world.
     * <p>
     * The value is silently clamped so it never exceeds the world's actual height bounds.
     * </p>
     *
     * @param worldName The name of the world.
     * @param type      {@link HeightType#MIN} or {@link HeightType#MAX}.
     * @param value     The desired height value.
     * @return {@code true} if the database update succeeded.
     */
    public abstract boolean setBuildHeight(String worldName, HeightType type, int value);

    /**
     * Returns the minimum corner of the island's bounding box in the given world.
     * <p>
     * This is the lowest point on the X/Y/Z axes: the X and Z coordinates are derived
     * from the island center minus half its size, while the Y coordinate uses the island's
     * custom minimum build height if set, otherwise the world's minimum height.
     * </p>
     *
     * @param world The {@link World} in which to compute the point.
     * @return The minimum {@link Location} of the island in the given world.
     */
    public abstract Location getMinimumPoint(World world);

    /**
     * Returns the maximum corner of the island's bounding box in the given world.
     * <p>
     * This is the highest point on the X/Y/Z axes: the X and Z coordinates are derived
     * from the island center plus half its size, while the Y coordinate uses the island's
     * custom maximum build height if set, otherwise the world's maximum height.
     * </p>
     *
     * @param world The {@link World} in which to compute the point.
     * @return The maximum {@link Location} of the island in the given world.
     */
    public abstract Location getMaximumPoint(World world);

    /**
     * Checks whether the given location lies within this island's bounds.
     * <p>
     * Convenience overload that extracts the world and block coordinates from the
     * location. Returns {@code false} if the location has no associated world.
     * </p>
     *
     * @param location The {@link Location} to test.
     * @return {@code true} if the location is inside the island, {@code false} otherwise.
     */
    public abstract boolean isInside(Location location);

    /**
     * Checks whether the given block coordinates lie within this island's bounds
     * in the specified world.
     * <p>
     * This is the allocation-free variant intended for hot code paths: it resolves the
     * island bounds (X/Y/Z) from a cache rather than recomputing them on each call.
     * </p>
     *
     * @param world  The {@link World} to test against.
     * @param blockX The block X coordinate.
     * @param blockY The block Y coordinate.
     * @param blockZ The block Z coordinate.
     * @return {@code true} if the coordinates are inside the island, {@code false} otherwise.
     */
    public abstract boolean isInside(@NotNull World world, int blockX, int blockY, int blockZ);
}