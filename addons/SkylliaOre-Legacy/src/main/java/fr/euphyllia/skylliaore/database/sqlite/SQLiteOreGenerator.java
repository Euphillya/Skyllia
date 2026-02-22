package fr.euphyllia.skylliaore.database.sqlite;

import fr.euphyllia.skyllia.sgbd.sqlite.SQLiteDatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import fr.euphyllia.skylliaore.SkylliaOre;
import fr.euphyllia.skylliaore.api.Generator;
import fr.euphyllia.skylliaore.api.OreGenerator;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class SQLiteOreGenerator implements OreGenerator {

    private static final String SELECT_GENERATOR_ISLAND = """
            SELECT generator_id
            FROM generators
            WHERE island_id = ?;
            """;

    private static final String UPSERT_GENERATOR_ISLAND = """
            INSERT INTO generators (island_id, generator_id)
            VALUES (?, ?)
            ON CONFLICT(island_id) DO UPDATE SET generator_id = excluded.generator_id;
            """;
    private final SQLiteDatabaseLoader loader;

    public SQLiteOreGenerator(SQLiteDatabaseLoader loader) {
        this.loader = loader;
    }

    @Override
    public Generator getGenIsland(UUID islandId) {
        return SQLExecute.queryMap(loader, SELECT_GENERATOR_ISLAND, List.of(islandId.toString()), resultSet -> {
            try {
                if (resultSet.next()) {
                    String generatorId = resultSet.getString("generator_id");
                    return SkylliaOre.getDefaultConfig().getGenerators().getOrDefault(generatorId,
                            SkylliaOre.getDefaultConfig().getDefaultGenerator());
                } else {
                    return SkylliaOre.getDefaultConfig().getDefaultGenerator();
                }
            } catch (SQLException e) {
                return SkylliaOre.getDefaultConfig().getDefaultGenerator();
            }
        });
    }

    @Override
    public Boolean updateGenIsland(UUID islandId, String generatorName) {
        int affected = SQLExecute.update(loader, UPSERT_GENERATOR_ISLAND, List.of(islandId.toString(), generatorName));
        return affected > 0;
    }
}
