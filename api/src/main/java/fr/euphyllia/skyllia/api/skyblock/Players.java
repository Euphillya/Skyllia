package fr.euphyllia.skyllia.api.skyblock;

import fr.euphyllia.skyllia.api.skyblock.model.RoleType;

import java.util.UUID;

/**
 * Represents a player in the Skyblock environment.
 */
public class Players {

    private final UUID mojangId;
    private final UUID islandId;
    private String lastKnowName;
    private RoleType roleType;

    /**
     * Constructs a new Players object.
     *
     * @param playerId   The UUID of the player.
     * @param playerName The last known name of the player.
     * @param islandId   The UUID of the island the player is associated with.
     * @param role       The role of the player on the island.
     */
    public Players(UUID playerId, String playerName, UUID islandId, RoleType role) {
        this.mojangId = playerId;
        this.lastKnowName = playerName;
        this.islandId = islandId;
        this.roleType = role;
    }

    /**
     * Gets the role type of the player on the island.
     *
     * @return The role type.
     */
    public RoleType getRoleType() {
        return roleType;
    }

    /**
     * Sets the role type of the player on the island.
     *
     * @param roleType The new role type.
     */
    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    /**
     * Gets the last known name of the player.
     *
     * @return The last known name.
     */
    public String getLastKnowName() {
        return lastKnowName;
    }

    /**
     * Sets the last known name of the player.
     *
     * @param lastKnowName The new last known name.
     */
    public void setLastKnowName(String lastKnowName) {
        this.lastKnowName = lastKnowName;
    }

    /**
     * Gets the Mojang UUID of the player.
     *
     * @return The Mojang UUID.
     */
    public UUID getMojangId() {
        return mojangId;
    }
}
