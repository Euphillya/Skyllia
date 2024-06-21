package fr.euphyllia.skyllia.api.skyblock.model;

/**
 * Represents different roles that a player can have on a Skyblock island.
 */
public enum RoleType {

    OWNER(4),
    CO_OWNER(3),
    MODERATOR(2),
    MEMBER(1),
    VISITOR(0),
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
