package fr.euphyllia.skylliaore.database.sqlite;

import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLite;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLiteDatabaseLoader;
import fr.euphyllia.skylliaore.api.OreGenerator;

public class SQLiteOreInit {

    private static SQLiteDatabaseLoader database;
    private static OreGenerator sqliteGenerator;

    public static SQLiteDatabaseLoader getDatabase() {
        return database;
    }

    public static OreGenerator getSqliteGenerator() {
        return sqliteGenerator;
    }

    public boolean init() throws DatabaseException {
        SQLite sqlite = new SQLite(ConfigLoader.database.getSqLiteConfig());
        database = new SQLiteDatabaseLoader(sqlite);
        if (!database.loadDatabase()) {
            return false;
        }

        // Créer la table si elle n'existe pas
        database.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS generators (
                        island_id TEXT PRIMARY KEY,
                        generator_id TEXT NOT NULL
                    );
                """, null, null, null);

        sqliteGenerator = new SQLiteOreGenerator(database);
        return true;
    }
}
