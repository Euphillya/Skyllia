package fr.euphyllia.skyllia.api.database;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract query class for managing per-island, per-world build height limits.
 * <p>
 * Each island can have a custom {@code min_height} and {@code max_height} per world.
 * When no custom value is set ({@code null}), the world's default height should be used.
 * The stored value can never exceed the world's actual min/max height.
 */
public abstract class IslandBuildHeightQuery {

    /**
     * Retrieves the custom minimum build height for the given island in the given world.
     *
     * @param island    The island.
     * @param worldName The name of the world.
     * @return The custom min height, or {@code null} if none is set (use world default).
     */
    public abstract @Nullable Integer getMinHeight(Island island, String worldName);

    /**
     * Retrieves the custom maximum build height for the given island in the given world.
     *
     * @param island    The island.
     * @param worldName The name of the world.
     * @return The custom max height, or {@code null} if none is set (use world default).
     */
    public abstract @Nullable Integer getMaxHeight(Island island, String worldName);

    /**
     * Sets the custom minimum build height for the given island in the given world.
     *
     * @param island    The island.
     * @param worldName The name of the world.
     * @param value     The new min height value.
     * @return {@code true} if the update succeeded.
     */
    public abstract boolean setMinHeight(Island island, String worldName, int value);

    /**
     * Sets the custom maximum build height for the given island in the given world.
     *
     * @param island    The island.
     * @param worldName The name of the world.
     * @param value     The new max height value.
     * @return {@code true} if the update succeeded.
     */
    public abstract boolean setMaxHeight(Island island, String worldName, int value);
}
