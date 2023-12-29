package fr.euphyllia.skyfolia.api.skyblock.model;

public enum RoleType {

    OWNER(3), MODERATOR(2), MEMBER(1), VISITOR(0), BAN(-1);

    private final int value;

    RoleType(int i) {
        this.value = i;
    }

    public static RoleType getRoleById(int id) {
        for (RoleType roleType : RoleType.values()) {
            if (roleType.getValue() == id) {
                return roleType;
            }
        }
        return RoleType.VISITOR;
    }

    public int getValue() {
        return this.value;
    }
}
