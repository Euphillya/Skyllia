package fr.euphyllia.skylliaore.database.postgresql;

import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import fr.euphyllia.skylliaore.SkylliaOre;
import fr.euphyllia.skylliaore.api.Generator;
import fr.euphyllia.skylliaore.api.OreGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class PostgreSQLGenerator implements OreGenerator {

    private static final Logger log = LogManager.getLogger(PostgreSQLGenerator.class);

    private static final String SELECT_GENERATOR_ISLAND = """
            SELECT generator_id
            FROM generators
            WHERE island_id = ?;
            """;

    private static final String UPSERT_GENERATOR_ISLAND = """
            INSERT INTO generators (island_id, generator_id)
            VALUES (?, ?)
            ON CONFLICT (island_id)
            DO UPDATE SET generator_id = EXCLUDED.generator_id;
            """;

    private final DatabaseLoader loader;

    public PostgreSQLGenerator(DatabaseLoader loader) {
        this.loader = loader;
    }

    @Override
    public Generator getGenIsland(UUID islandId) {
        return SQLExecute.queryMap(loader, SELECT_GENERATOR_ISLAND, List.of(islandId.toString()), rs -> {
            try {
                if (rs.next()) {
                    String generatorId = rs.getString("generator_id");
                    return SkylliaOre.getDefaultConfig().getGenerators().getOrDefault(
                            generatorId,
                            SkylliaOre.getDefaultConfig().getDefaultGenerator()
                    );
                }
                return SkylliaOre.getDefaultConfig().getDefaultGenerator();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
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
