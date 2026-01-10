package fr.euphyllia.skyllia.api.permissions;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import org.bukkit.entity.Player;

public class PermissionsManagers {


    public boolean hasPermission(Player player, Island island, PermissionId permission) {
        var member = island.getMember(player.getUniqueId());
        RoleType role = member != null ? member.getRoleType() : RoleType.VISITOR;
        if (role == null) role = RoleType.VISITOR;

        if (role == RoleType.OWNER) return true;
        if (role == RoleType.BAN) return false;

        var compiled = island.getCompiledPermissions();
        return compiled.has(SkylliaAPI.getPermissionRegistry(), role, permission);
    }

    public boolean hasIslandFlag(Island island, PermissionId flag) {
        if (flag == null) return false;
        var compiled = island.getCompiledPermissions();
        return compiled.has(
                SkylliaAPI.getPermissionRegistry(),
                RoleType.ISLAND_FLAGS,
                flag
        );
    }

    public boolean hasIslandFlag(Island island, PermissionId specific, PermissionId fallback) {
        var compiled = island.getCompiledPermissions();
        var registry = SkylliaAPI.getPermissionRegistry();

        return compiled.has(registry, RoleType.ISLAND_FLAGS, specific)
                || compiled.has(registry, RoleType.ISLAND_FLAGS, fallback);
    }


}
