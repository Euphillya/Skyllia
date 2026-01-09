package fr.euphyllia.skyllia.api.permissions;

import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.database.mariadb.MariaDBPermissionIndexStore;
import fr.euphyllia.skyllia.database.postgresql.PostgreSQLPermissionIndexStore;
import fr.euphyllia.skyllia.database.sqlite.SQLitePermissionIndexStore;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;

public final class PermissionIndexStoreFactory {

    private PermissionIndexStoreFactory() {}

    public static PermissionIndexStore create(DatabaseLoader loader) {
        if (ConfigLoader.database.getPostgreConfig() != null) {
            return new PostgreSQLPermissionIndexStore(loader,  "public");
        }
        if (ConfigLoader.database.getMariaDBConfig() != null) {
            return new MariaDBPermissionIndexStore(loader);
        }
        if (ConfigLoader.database.getSqLiteConfig() != null) {
            return new SQLitePermissionIndexStore(loader);
        }

        throw new IllegalStateException("No database configured for PermissionIndexStore");
    }
}
