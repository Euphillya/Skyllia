package fr.euphyllia.skyllia.api.skyblock.model;

/**
 * Represents different roles that a player can have on a Skyblock island.
 */
public enum RoleType {

    /**
     * Role with the highest level of authority, typically the owner of the island.
     */
    OWNER(4),

    /**
     * Role with significant authority, often a co-owner or trusted individual with many privileges.
     */
    CO_OWNER(3),

    /**
     * Role with moderate authority, usually a moderator responsible for managing the island.
     */
    MODERATOR(2),

    /**
     * Role with basic access, typically a regular member of the island.
     */
    MEMBER(1),

    /**
     * Role with minimal access, often a visitor with very limited permissions.
     */
    VISITOR(0),

    /**
     * Role indicating that the player is banned from the island.
     */
    BAN(-1);

    private final int value;

    /**
     * Constructs a RoleType with the specified value.
     *
     * @param i The value associated with the role.
     */
    RoleType(int i) {
        this.value = i;
    }

    /**
     * Gets the RoleType associated with the specified ID.
     *
     * @param id The ID of the role.
     * @return The RoleType corresponding to the ID, or VISITOR if not found.
     */
    public static RoleType getRoleById(int id) {
        for (RoleType roleType : RoleType.values()) {
            if (roleType.getValue() == id) {
                return roleType;
            }
        }
        return RoleType.VISITOR;
    }

    /**
     * Gets the value associated with this role.
     *
     * @return The value of the role.
     */
    public int getValue() {
        return this.value;
    }
}
