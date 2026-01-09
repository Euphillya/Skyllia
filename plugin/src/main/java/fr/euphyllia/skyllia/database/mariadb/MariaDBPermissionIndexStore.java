package fr.euphyllia.skyllia.database.mariadb;

import fr.euphyllia.skyllia.api.permissions.PermissionIndexStore;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;

import java.util.List;

public final class MariaDBPermissionIndexStore implements PermissionIndexStore {

    private final DatabaseLoader databaseLoader;

    private final String SELECT_IDX = "SELECT idx FROM permission_registry WHERE node = ?;";
    private final String INSERT_NODE = "INSERT IGNORE INTO permission_registry (node) VALUES (?);";

    public MariaDBPermissionIndexStore(DatabaseLoader databaseLoader) {
        this.databaseLoader = databaseLoader;
    }

    @Override
    public int getOrAllocate(String nodeKey) {
        Integer existing = selectIdx(nodeKey);
        if (existing != null) return existing;

        SQLExecute.update(databaseLoader, INSERT_NODE, List.of(nodeKey));

        existing = selectIdx(nodeKey);
        if (existing == null) {
            throw new IllegalStateException("Failed to allocate permission index for node=" + nodeKey);
        }
        return existing;
    }

    private Integer selectIdx(String nodeKey) {
        return SQLExecute.queryMap(databaseLoader, SELECT_IDX, List.of(nodeKey), rs -> {
            try {
                if (!rs.next()) return null;
                return rs.getInt("idx");
            } catch (Exception e) {
                return null;
            }
        });
    }
}
