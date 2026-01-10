package fr.euphyllia.skyllia.api.database;

import fr.euphyllia.skyllia.api.skyblock.Island;

/**
 * The {@code IslandUpdateQuery} class defines an abstract set of methods
 * for updating various properties of an {@link Island} in a SkyBlock context.
 * <p>
 * Implementations should handle operations such as toggling the island status
 * (disable/private), adjusting maximum members, and resizing the island.
 */
public abstract class IslandUpdateQuery {

    public abstract Boolean updateDisable(Island island, boolean disable);

    public abstract Boolean updatePrivate(Island island, boolean privateIsland);

    public abstract Boolean isDisabledIsland(Island island);

    public abstract Boolean isPrivateIsland(Island island);

    public abstract Boolean setMaxMemberInIsland(Island island, int newValue);

    public abstract Boolean setSizeIsland(Island island, double newValue);

    public abstract Boolean setLockedIsland(Island island, boolean locked);

    public abstract Boolean isLockedIsland(Island island);
}
