package fr.euphyllia.skylliaore.database.mariadb;

import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import fr.euphyllia.skylliaore.SkylliaOre;
import fr.euphyllia.skylliaore.api.Generator;
import fr.euphyllia.skylliaore.api.OreGenerator;
import fr.euphyllia.skylliaore.config.DefaultConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class MariaDBGenerator implements OreGenerator {

    private static final Logger log = LogManager.getLogger(MariaDBGenerator.class);

    private static final String SELECT_GENERATOR_ISLAND = """
            SELECT generator_id
            FROM `generators`
            WHERE island_id = ?;
            """;

    private static final String UPSERT_GENERATOR_ISLAND = """
            INSERT INTO `generators` (island_id, generator_id)
            VALUES (?, ?)
            ON DUPLICATE KEY UPDATE generator_id = VALUES(generator_id);
            """;

    private final DatabaseLoader mariaDBLoader;

    public MariaDBGenerator(DatabaseLoader loader) {
        mariaDBLoader = loader;
    }

    public Generator getGenIsland(UUID islandId) {
        return SQLExecute.queryMap(mariaDBLoader, SELECT_GENERATOR_ISLAND, List.of(islandId), resultSet -> {
            try {
                if (resultSet.next()) {
                    String generatorId = resultSet.getString("generator_id");
                    return getGeneratorById(generatorId);
                } else {
                    return getDefaultGenerator();
                }
            } catch (SQLException exception) {
                log.error(exception.getMessage(), exception);
                return getDefaultGenerator();
            }
        });
    }

    public Boolean updateGenIsland(UUID islandId, String generatorName) {
        int affected = SQLExecute.update(mariaDBLoader, UPSERT_GENERATOR_ISLAND,
                List.of(islandId, generatorName));
        return affected > 0;
    }

    private Generator getGeneratorById(String generatorId) {
        DefaultConfig config = SkylliaOre.getDefaultConfig();
        return config.getGenerators().getOrDefault(generatorId,
                getDefaultGenerator());
    }

    private Generator getDefaultGenerator() {
        return SkylliaOre.getDefaultConfig().getDefaultGenerator();
    }
}
