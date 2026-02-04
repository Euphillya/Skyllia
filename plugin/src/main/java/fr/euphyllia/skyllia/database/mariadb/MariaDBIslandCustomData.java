package fr.euphyllia.skyllia.database.mariadb;

import fr.euphyllia.skyllia.api.database.IslandCustomDataQuery;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MariaDBIslandCustomData extends IslandCustomDataQuery {

    private static final Logger log = LogManager.getLogger(MariaDBIslandCustomData.class);
    private final DatabaseLoader databaseLoader;
    private final ConcurrentHashMap<String, Boolean> tablesCreated = new ConcurrentHashMap<>();
    private final PersistentDataAdapterContext adapterContext = new EmptyAdapterContext();

    public MariaDBIslandCustomData(DatabaseLoader databaseLoader) {
        this.databaseLoader = databaseLoader;
    }

    private boolean ensureTableExists(NamespacedKey namespace) {
        String tableName = getTableName(namespace);

        if (tablesCreated.containsKey(tableName)) {
            return true;
        }

        String createTableSQL = String.format("""
                CREATE TABLE IF NOT EXISTS %s (
                    island_id CHAR(36) NOT NULL,
                    data_key VARCHAR(255) NOT NULL,
                    data_value LONGBLOB NOT NULL,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (island_id, data_key),
                    INDEX idx_island_id (island_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
                """, tableName);

        int affected = SQLExecute.update(databaseLoader, createTableSQL, null);
        if (affected >= 0) {
            tablesCreated.put(tableName, true);
            return true;
        }
        log.error("Failed to create custom data table: {}", tableName);
        return false;
    }

    @Override
    public <P, C> boolean set(@NotNull NamespacedKey namespace, @NotNull Island island, @NotNull String dataKey, @NotNull PersistentDataType<P, C> type, @NotNull C value) {
        if (!ensureTableExists(namespace)) return false;

        String tableName = getTableName(namespace);
        String sql = String.format("""
                INSERT INTO %s (island_id, data_key, data_value)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    data_value = VALUES(data_value),
                    updated_at = CURRENT_TIMESTAMP
                """, tableName);

        try {
            P primitive = type.toPrimitive(value, adapterContext);
            byte[] serialized = serializePrimitive(primitive);

            int affected = SQLExecute.update(databaseLoader, sql, List.of(
                    island.getId().toString(),
                    dataKey,
                    serialized
            ));
            return affected > 0;
        } catch (Exception e) {
            log.error("Failed to store data for key: {}", dataKey, e);
            return false;
        }
    }

    @Override
    public @Nullable <P, C> C get(@NotNull NamespacedKey namespace, @NotNull Island island, @NotNull String dataKey, @NotNull PersistentDataType<P, C> type) {
        String tableName = getTableName(namespace);
        String sql = String.format("""
                SELECT data_value FROM %s
                WHERE island_id = ? AND data_key = ?
                """, tableName);

        return SQLExecute.queryMap(databaseLoader, sql, List.of(island.getId().toString(), dataKey), rs -> {
            try {
                if (rs.next()) {
                    byte[] serialized = rs.getBytes("data_value");
                    P primitive = deserializePrimitive(serialized, type.getPrimitiveType());
                    return type.fromPrimitive(primitive, adapterContext);
                }
            } catch (Exception e) {
                log.error("Failed to retrieve data for key: {}", dataKey, e);
            }
            return null;
        });
    }

    @Override
    public @NotNull <P, C> C getOrDefault(@NotNull NamespacedKey namespace, @NotNull Island island, @NotNull String dataKey, @NotNull PersistentDataType<P, C> type, @NotNull C defaultValue) {
        C value = get(namespace, island, dataKey, type);
        return value != null ? value : defaultValue;
    }

    @Override
    public boolean has(@NotNull NamespacedKey namespace, @NotNull Island island, @NotNull String dataKey) {
        String tableName = getTableName(namespace);
        String sql = String.format("""
                SELECT 1 FROM %s
                WHERE island_id = ? AND data_key = ?
                LIMIT 1
                """, tableName);

        Boolean result = SQLExecute.queryMap(databaseLoader, sql, List.of(island.getId().toString(), dataKey), rs -> {
            try {
                return rs.next();
            } catch (SQLException e) {
                log.error("Failed to check if data exists for key: {}", dataKey, e);
                return false;
            }
        });
        return result != null && result;
    }

    @Override
    public <P, C> boolean has(@NotNull NamespacedKey namespace, @NotNull Island island, @NotNull String dataKey, @NotNull PersistentDataType<P, C> type) {
        try {
            return get(namespace, island, dataKey, type) != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public @NotNull Set<String> getKeys(@NotNull NamespacedKey namespace, @NotNull Island island) {
        String tableName = getTableName(namespace);
        String sql = String.format("""
                SELECT data_key FROM %s
                WHERE island_id = ?
                """, tableName);

        Set<String> keys = SQLExecute.queryMap(databaseLoader, sql, List.of(island.getId().toString()), rs -> {
            Set<String> result = new HashSet<>();
            try {
                while (rs.next()) {
                    result.add(rs.getString("data_key"));
                }
            } catch (SQLException e) {
                log.error("Failed to retrieve keys", e);
            }
            return result;
        });
        return keys != null ? keys : new HashSet<>();
    }

    @Override
    public boolean remove(@NotNull NamespacedKey namespace, @NotNull Island island, @NotNull String dataKey) {
        String tableName = getTableName(namespace);
        String sql = String.format("""
                DELETE FROM %s
                WHERE island_id = ? AND data_key = ?
                """, tableName);

        int affected = SQLExecute.update(databaseLoader, sql, List.of(island.getId().toString(), dataKey));
        return affected > 0;
    }

    @Override
    public boolean clear(@NotNull NamespacedKey namespace, @NotNull Island island) {
        String tableName = getTableName(namespace);
        String sql = String.format("""
                DELETE FROM %s
                WHERE island_id = ?
                """, tableName);

        int affected = SQLExecute.update(databaseLoader, sql, List.of(island.getId().toString()));
        return affected >= 0;
    }

    @Override
    public int size(@NotNull NamespacedKey namespace, @NotNull Island island) {
        String tableName = getTableName(namespace);
        String sql = String.format("""
                SELECT COUNT(*) FROM %s
                WHERE island_id = ?
                """, tableName);

        Integer count = SQLExecute.queryMap(databaseLoader, sql, List.of(island.getId().toString()), rs -> {
            try {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                log.error("Failed to get size", e);
            }
            return 0;
        });
        return count != null ? count : 0;
    }

    @Override
    public boolean isEmpty(@NotNull NamespacedKey namespace, @NotNull Island island) {
        return size(namespace, island) == 0;
    }
}
