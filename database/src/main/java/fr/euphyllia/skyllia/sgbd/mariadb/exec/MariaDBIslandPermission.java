package fr.euphyllia.skyllia.sgbd.mariadb.exec;

import fr.euphyllia.skyllia.api.database.IslandPermissionQuery;
import fr.euphyllia.skyllia.api.skyblock.Island;

public class MariaDBIslandPermission extends IslandPermissionQuery {
    @Override
    public Long getIslandGameRule(Island island) {
        return 0L;
    }

    @Override
    public Boolean updateIslandGameRule(Island island, long value) {
        return null;
    }
}
