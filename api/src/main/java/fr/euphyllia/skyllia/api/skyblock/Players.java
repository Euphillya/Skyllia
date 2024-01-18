package fr.euphyllia.skyllia.api.skyblock;

import fr.euphyllia.skyllia.api.skyblock.model.RoleType;

import java.util.UUID;

public class Players {

    private final UUID mojangId;
    private final UUID islandId;
    private String lastKnowName;
    private RoleType roleType;

    public Players(UUID playerId, String playerName, UUID islandId, RoleType role) {
        this.mojangId = playerId;
        this.lastKnowName = playerName;
        this.islandId = islandId;
        this.roleType = role;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    public String getLastKnowName() {
        return lastKnowName;
    }

    public void setLastKnowName(String lastKnowName) {
        this.lastKnowName = lastKnowName;
    }

    public UUID getMojangId() {
        return mojangId;
    }
}
