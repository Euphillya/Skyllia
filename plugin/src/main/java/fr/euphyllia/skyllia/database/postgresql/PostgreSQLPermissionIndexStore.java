package fr.euphyllia.skyllia.database.postgresql;

import fr.euphyllia.skyllia.api.permissions.PermissionIndexStore;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class PostgreSQLPermissionIndexStore implements PermissionIndexStore {

    private final DatabaseLoader databaseLoader;
    private final String schema;

    private final String SELECT_IDX;
    private final String INSERT_NODE;

    public PostgreSQLPermissionIndexStore(@NotNull DatabaseLoader databaseLoader, @NotNull String schema) {
        this.databaseLoader = databaseLoader;
        this.schema = sanitizeIdent(schema);

        String table = this.schema + ".permission_registry";

        this.SELECT_IDX = """
                SELECT idx
                FROM %s
                WHERE node = ?;
                """.formatted(table);

        this.INSERT_NODE = """
                INSERT INTO %s (node)
                VALUES (?)
                ON CONFLICT (node) DO NOTHING;
                """.formatted(table);
    }

    private static String sanitizeIdent(String ident) {
        if (ident == null || ident.isBlank()) return "public";
        return ident.replaceAll("[^a-zA-Z0-9_]", "");
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
