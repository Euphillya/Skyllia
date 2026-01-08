package fr.euphyllia.skyllia.database.mariadb;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.database.IslandPermissionQuery;
import fr.euphyllia.skyllia.api.permissions.CompiledPermissions;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.permissions.PermissionSet;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MariaDBIslandPermission extends IslandPermissionQuery {

    private static final Logger log = LoggerFactory.getLogger(MariaDBIslandPermission.class);

    private static final String SELECT_ALL = """
            SELECT role, words
            FROM islands_permissions_v2
            WHERE island_id = ?;
            """;

    private static final String UPSERT_ROLE = """
            INSERT INTO islands_permissions_v2 (island_id, role, words)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE words = VALUES(words);
            """;

    private static final String DELETE_ROLE = """
            DELETE FROM islands_permissions_v2
            WHERE island_id = ? AND role = ?;
            """;

    private final DatabaseLoader databaseLoader;

    public MariaDBIslandPermission(DatabaseLoader databaseLoader) {
        this.databaseLoader = databaseLoader;
    }

    // long[] -> byte[] (little-endian)
    private static byte[] encodeLongArray(long[] words) {
        if (words == null || words.length == 0) return new byte[0];
        ByteBuffer buf = ByteBuffer.allocate(words.length * Long.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        for (long w : words) buf.putLong(w);
        return buf.array();
    }

    // byte[] -> long[] (little-endian)
    private static long[] decodeLongArray(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return new long[0];
        int longs = bytes.length / Long.BYTES;
        if (longs <= 0) return new long[0];

        ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        long[] out = new long[longs];
        for (int i = 0; i < longs; i++) out[i] = buf.getLong();
        return out;
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

        for (RoleRow row : rows) {
            RoleType role;
            try {
                role = RoleType.valueOf(row.role());
            } catch (Exception ignored) {
                continue;
            }

            long[] wordsArr = decodeLongArray(row.words());
            PermissionSet set = new PermissionSet(registry.size());
            set.loadWords(wordsArr);
            set.ensureCapacity(registry.size());
            compiled.replace(role, set);
        }

        compiled.ensureUpToDate(registry);
        return compiled;
    }

    @Override
    public boolean set(UUID islandId, RoleType role, PermissionId id, boolean value) {
        // Simple & sûr (mais IO): load -> set -> saveRole
        PermissionRegistry registry = SkylliaAPI.getPermissionRegistry();
        CompiledPermissions compiled = loadCompiled(islandId, registry);
        if (compiled == null) return false;

        PermissionSet set = compiled.setFor(role);
        if (set == null) return false;

        set.set(id, value);
        byte[] blob = encodeLongArray(set.snapshotWords());
        return saveRole(islandId, role, blob);
    }

    /* ---------------- serialization helpers ---------------- */

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
