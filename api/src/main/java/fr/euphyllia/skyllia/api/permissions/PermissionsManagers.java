package fr.euphyllia.skyllia.api.permissions;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import org.bukkit.entity.Player;

public class PermissionsManagers {


    public boolean hasPermission(Player player, Island island, PermissionId permission) {
        RoleType role = island.getMember(player.getUniqueId()).getRoleType(); // Todo : Fait actuellement une requête à chaque fois à la db, et pour les testes, je veux la valeur exact
        if (role == RoleType.OWNER) {
            return true;
        }
        if (role == RoleType.BAN) {
            return false;
        }

        var compiled = island.getCompiledPermissions();
        return compiled.has(SkylliaAPI.getPermissionRegistry(), role, permission);

    }
}
