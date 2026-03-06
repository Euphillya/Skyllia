package fr.euphyllia.skyllia.database.postgresql;

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

public class PostgreSQLIslandPermission extends IslandPermissionQuery {

    private static final Logger log = LoggerFactory.getLogger(PostgreSQLIslandPermission.class);

    private final DatabaseLoader databaseLoader;
    private final String schema;

    private final String SELECT_ALL;
    private final String UPSERT_ROLE;
    private final String DELETE_ROLE;


    private final String SELECT_FLAGS;
    private final String UPSERT_FLAGS;

    public PostgreSQLIslandPermission(DatabaseLoader databaseLoader) {
        this(databaseLoader, "public");
    }

    public PostgreSQLIslandPermission(DatabaseLoader databaseLoader, String schema) {
        this.databaseLoader = databaseLoader;
        this.schema = sanitizeIdent(schema);

        String permTable = this.schema + ".islands_permissions_v2";
        String flagsTable = this.schema + ".islands_flags";

        this.SELECT_ALL = """
                SELECT role, words
                FROM %s
                WHERE island_id = ?;
                """.formatted(permTable);

        this.UPSERT_ROLE = """
                INSERT INTO %s (island_id, role, words)
                VALUES (?, ?, ?)
                ON CONFLICT (island_id, role)
                DO UPDATE SET words = EXCLUDED.words;
                """.formatted(permTable);

        this.DELETE_ROLE = """
                DELETE FROM %s
                WHERE island_id = ? AND role = ?;
                """.formatted(permTable);

        this.SELECT_FLAGS = """
                SELECT words
                FROM %s
                WHERE island_id = ?;
                """.formatted(flagsTable);

        this.UPSERT_FLAGS = """
                INSERT INTO %s (island_id, words)
                VALUES (?, ?)
                ON CONFLICT (island_id)
                DO UPDATE SET words = EXCLUDED.words;
                """.formatted(flagsTable);
    }

    private static String sanitizeIdent(String ident) {
        if (ident == null || ident.isBlank()) return "public";
        return ident.replaceAll("[^a-zA-Z0-9_]", "");
    }

    @Override
    public @Nullable CompiledPermissions loadCompiled(UUID islandId, PermissionRegistry registry) {
        CompiledPermissions compiled = new CompiledPermissions(registry);

        List<RoleRow> rows = SQLExecute.queryMap(databaseLoader, SELECT_ALL, List.of(islandId), rs -> {
            List<RoleRow> out = new ArrayList<>();
            try {
                while (rs.next()) {
                    String roleStr = rs.getString("role");
                    byte[] words = rs.getBytes("words");
                    out.add(new RoleRow(roleStr, words));
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

    @Override
    public boolean set(UUID islandId, RoleType role, PermissionId id, boolean value) {
        return super.set(islandId, SkylliaAPI.getPermissionRegistry(), role, id, value);
    }

    @Override
    public boolean saveRole(UUID islandId, RoleType role, byte[] wordsBlob) {
        int affected = SQLExecute.update(databaseLoader, UPSERT_ROLE,
                List.of(islandId, role.name(), wordsBlob));
        return affected != 0;
    }

    @Override
    public boolean deleteRole(UUID islandId, RoleType role) {
        int affected = SQLExecute.update(databaseLoader, DELETE_ROLE,
                List.of(islandId, role.name()));
        return affected != 0;
    }

    @Override
    public @Nullable IslandFlags loadIslandFlags(UUID islandId, IslandFlagRegistry registry) {
        byte[] words = SQLExecute.queryMap(databaseLoader, SELECT_FLAGS, List.of(islandId), rs -> {
            try {
                if (rs.next()) return rs.getBytes("words");
            } catch (SQLException e) {
                log.error("SQL error while loading flags for island {}", islandId, e);
            }
            return null;
        });

        if (words == null) return null;

        IslandFlags flags = new IslandFlags(registry);
        flags.loadWords(PermissionSetCodec.decodeLongs(words));
        flags.ensureUpToDate(registry);
        return flags;
    }

    @Override
    public boolean saveIslandFlags(UUID islandId, byte[] wordsBlob) {
        int affected = SQLExecute.update(databaseLoader, UPSERT_FLAGS,
                List.of(islandId, wordsBlob));
        return affected != 0;
    }

    private record RoleRow(String role, byte[] words) {
    }
}
