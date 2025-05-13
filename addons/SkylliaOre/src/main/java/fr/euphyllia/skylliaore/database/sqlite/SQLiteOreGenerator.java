package fr.euphyllia.skylliaore.database.sqlite;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLiteDatabaseLoader;
import fr.euphyllia.skylliaore.SkylliaOre;
import fr.euphyllia.skylliaore.api.Generator;
import fr.euphyllia.skylliaore.api.OreGenerator;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SQLiteOreGenerator implements OreGenerator {

    private final SQLiteDatabaseLoader loader;

    public SQLiteOreGenerator(SQLiteDatabaseLoader loader) {
        this.loader = loader;
    }

    @Override
    public CompletableFuture<Generator> getGenIsland(UUID islandId) {
        CompletableFuture<Generator> future = new CompletableFuture<>();
        try {
            loader.executeQuery("SELECT generator_id FROM generators WHERE island_id = ?;",
                    List.of(islandId.toString()), resultSet -> {
                        try {
                            if (resultSet.next()) {
                                String generatorId = resultSet.getString("generator_id");
                                Generator generator = SkylliaOre.getDefaultConfig().getGenerators().getOrDefault(generatorId,
                                        SkylliaOre.getDefaultConfig().getDefaultGenerator());
                                future.complete(generator);
                            } else {
                                future.complete(SkylliaOre.getDefaultConfig().getDefaultGenerator());
                            }
                        } catch (SQLException e) {
                            future.complete(SkylliaOre.getDefaultConfig().getDefaultGenerator());
                        }
                    }, null);
        } catch (DatabaseException e) {
            future.complete(SkylliaOre.getDefaultConfig().getDefaultGenerator());
        }
        return future;
    }

    @Override
    public CompletableFuture<Boolean> updateGenIsland(UUID islandId, String generatorName) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        String query = """
                INSERT INTO generators (island_id, generator_id)
                VALUES (?, ?)
                ON CONFLICT(island_id) DO UPDATE SET generator_id = excluded.generator_id;
                """;
        try {
            loader.executeUpdate(query, List.of(islandId.toString(), generatorName),
                    affected -> future.complete(affected > 0), null);
        } catch (DatabaseException e) {
            future.complete(false);
        }
        return future;
    }
}
