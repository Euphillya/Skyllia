package fr.euphyllia.skyllia.api.permissions;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Manages permission and flag checks for Skyllia islands.
 * <p>
 * Provides methods to verify whether a player holds a given {@link PermissionId}
 * on an island, and whether an island has a specific {@link FlagId} enabled.
 * </p>
 */
public interface PermissionsManagers {

    /**
     * Checks whether the given player has the specified permission on the island.
     * No Bukkit permission bypass is applied and debug logging is disabled.
     *
     * @param player     the player to check.
     * @param island     the island on which the permission is checked.
     * @param permission the permission to verify.
     * @return {@code true} if the player has the permission, {@code false} otherwise.
     */
    boolean hasPermission(Player player, Island island, PermissionId permission);

    /**
     * Checks whether the given player has the specified permission on the island,
     * with an optional Bukkit permission bypass. Debug logging is disabled.
     *
     * @param player           the player to check.
     * @param island           the island on which the permission is checked.
     * @param permission       the permission to verify.
     * @param bukkitPermission an optional Bukkit permission node; if the player holds it, access is granted immediately.
     * @return {@code true} if the player has the permission, {@code false} otherwise.
     */
    boolean hasPermission(Player player, Island island, PermissionId permission, @Nullable String bukkitPermission);

    /**
     * Checks whether the given player has the specified permission on the island,
     * with an optional Bukkit permission bypass and optional debug logging.
     * <p>
     * Resolution order:
     * <ol>
     *   <li>If {@code bukkitPermission} is non-null and the player holds it, access is granted immediately.</li>
     *   <li>If the player's role is {@link RoleType#OWNER}, access is always granted.</li>
     *   <li>If the player's role is {@link RoleType#BAN}, access is always denied.</li>
     *   <li>Otherwise, the island's compiled permissions are consulted for the player's role.</li>
     * </ol>
     * </p>
     *
     * @param player           the player to check.
     * @param island           the island on which the permission is checked.
     * @param permission       the permission to verify.
     * @param bukkitPermission an optional Bukkit permission node; if the player holds it, access is granted immediately.
     * @param debug            if {@code true}, detailed resolution steps are logged at INFO level.
     * @return {@code true} if the player has the permission, {@code false} otherwise.
     */
    boolean hasPermission(Player player, Island island, PermissionId permission, @Nullable String bukkitPermission, boolean debug);

    /**
     * Checks whether the given island has the specified flag enabled in the given world.
     *
     * @param island    the island to check.
     * @param flag      the flag to verify; if {@code null}, {@code false} is returned immediately.
     * @param worldName the name of the world in which to check the flag.
     * @return {@code true} if the flag is enabled on the island in the specified world, {@code false} otherwise.
     */
    boolean hasFlag(Island island, FlagId flag, String worldName);

    /**
     * Checks whether the given island has either the specific flag or the fallback flag enabled in the given world.
     *
     * @param island    the island to check.
     * @param specific  the primary flag to verify.
     * @param fallback  the fallback flag to verify if the primary is not set.
     * @param worldName the name of the world in which to check the flags.
     * @return {@code true} if either flag is enabled on the island in the specified world, {@code false} otherwise.
     */
    boolean hasFlag(Island island, FlagId specific, FlagId fallback, String worldName);

    /**
     * @deprecated Use {@link #hasFlag(Island, FlagId, String)} instead.
     */
    @Deprecated(forRemoval = true, since = "3.x")
    @ApiStatus.ScheduledForRemoval(inVersion = "4.x")
    default boolean hasFlag(Island island, FlagId flag) {
        List<WorldConfig> worlds = SkylliaAPI.getRegisteredWorlds();
        if (worlds.isEmpty()) return false;
        return this.hasFlag(island, flag, worlds.getFirst().getWorldName());
    }

    /**
     * @deprecated Use {@link #hasFlag(Island, FlagId, FlagId, String)} instead.
     */
    @Deprecated(forRemoval = true, since = "3.x")
    @ApiStatus.ScheduledForRemoval(inVersion = "4.x")
    default boolean hasFlag(Island island, FlagId specific, FlagId fallback) {
        List<WorldConfig> worlds = SkylliaAPI.getRegisteredWorlds();
        if (worlds.isEmpty()) return false;
        return this.hasFlag(island, specific, fallback, worlds.getFirst().getWorldName());
    }

}