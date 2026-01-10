package fr.euphyllia.skyllia.database.sqlite;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.database.IslandPermissionQuery;
import fr.euphyllia.skyllia.api.permissions.*;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLiteIslandPermission extends IslandPermissionQuery {

    private static final Logger log = LoggerFactory.getLogger(SQLiteIslandPermission.class);

    private static final String SELECT_ALL = """
            SELECT role, words
            FROM islands_permissions_v2
            WHERE island_id = ?;
            """;

    // SQLite UPSERT (requires UNIQUE/PK on (island_id, role))
    private static final String UPSERT_ROLE = """
            INSERT INTO islands_permissions_v2 (island_id, role, words)
            VALUES (?, ?, ?)
            ON CONFLICT(island_id, role)
            DO UPDATE SET words = excluded.words;
            """;

    private static final String DELETE_ROLE = """
            DELETE FROM islands_permissions_v2
            WHERE island_id = ? AND role = ?;
            """;

    private final DatabaseLoader databaseLoader;

    public SQLiteIslandPermission(DatabaseLoader databaseLoader) {
        this.databaseLoader = databaseLoader;
    }

    @Override
    public @Nullable CompiledPermissions loadCompiled(UUID islandId, PermissionRegistry registry) {
        CompiledPermissions compiled = new CompiledPermissions(registry);

        List<RoleRow> rows = SQLExecute.queryMap(databaseLoader, SELECT_ALL, List.of(islandId.toString()), rs -> {
            List<RoleRow> out = new ArrayList<>();
            try {
                while (rs.next()) {
                    out.add(new RoleRow(
                            rs.getString("role"),
                            rs.getBytes("words")
                    ));
                }
            } catch (SQLException e) {
                log.error("SQL error while loading permissions for island {}", islandId, e);
                return null;
            }
            return out;
        });

        if (rows == null) return null;

        int regSize = registry.size();

        for (RoleRow row : rows) {
            RoleType role;
            try {
                role = RoleType.valueOf(row.role());
            } catch (Exception ignored) {
                continue;
            }

            long[] wordsArr = PermissionSetCodec.decodeLongs(row.words());
            PermissionSet set = new PermissionSet(regSize);
            set.loadWords(wordsArr);
            set.ensureCapacity(regSize);

            compiled.replace(role, set);
        }

        compiled.ensureUpToDate(registry);
        return compiled;
    }

    /**
     * DB-only write. Runtime update is handled by your command/service after DB success.
     */
    @Override
    public boolean set(UUID islandId, RoleType role, PermissionId id, boolean value) {
        return super.set(islandId, SkylliaAPI.getPermissionRegistry(), role, id, value);
    }

    @Override
    public boolean saveRole(UUID islandId, RoleType role, byte[] wordsBlob) {
        int affected = SQLExecute.update(
                databaseLoader,
                UPSERT_ROLE,
                List.of(islandId.toString(), role.name(), wordsBlob)
        );
        return affected != 0;
    }

    @Override
    public boolean deleteRole(UUID islandId, RoleType role) {
        int affected = SQLExecute.update(
                databaseLoader,
                DELETE_ROLE,
                List.of(islandId.toString(), role.name())
        );
        return affected != 0;
    }

    private record RoleRow(String role, byte[] words) {
    }
}
