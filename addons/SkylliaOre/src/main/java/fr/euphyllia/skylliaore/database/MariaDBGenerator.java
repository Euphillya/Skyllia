package fr.euphyllia.skylliaore.database;

import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.mariadb.execute.MariaDBExecute;
import fr.euphyllia.skylliaore.SkylliaOre;
import fr.euphyllia.skylliaore.api.Generator;
import fr.euphyllia.skylliaore.config.DefaultConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MariaDBGenerator {

    private static final Logger log = LogManager.getLogger(MariaDBGenerator.class);

    private static final String SELECT_GENERATOR_ISLAND = """
            SELECT `island_id`, `generator_id`
            FROM `%s`.`generators`
            WHERE `island_id` = ?;
            """;

    private static final String UPSERT_GENERATOR_ISLAND = """
            INSERT INTO `%s`.`generators`
            (`island_id`, `generator_id`)
            VALUES(?, ?)
            ON DUPLICATE KEY UPDATE `generator_id` = VALUES(`generator_id`);
            """;

    private final String databaseName;

    public MariaDBGenerator() {
        this.databaseName = ConfigLoader.database.getMariaDBConfig().database();
    }

    public CompletableFuture<Generator> getGenIsland(UUID islandId) {
        CompletableFuture<Generator> future = new CompletableFuture<>();
        try {
            String query = SELECT_GENERATOR_ISLAND.formatted(this.databaseName);
            MariaDBExecute.executeQuery(MariaDBInit.getPool(), query, List.of(islandId), resultSet -> {
                try {
                    if (resultSet.next()) {
                        String generatorId = resultSet.getString("generator_id");
                        Generator generator = getGeneratorById(generatorId);
                        future.complete(generator);
                    } else {
                        future.complete(getDefaultGenerator());
                    }
                } catch (SQLException exception) {
                    log.error(exception.getMessage(), exception);
                    future.complete(getDefaultGenerator());
                }
            }, null);
        } catch (DatabaseException exception) {
            log.error(exception.getMessage(), exception);
            future.complete(getDefaultGenerator());
        }
        return future;
    }

    public CompletableFuture<Boolean> updateGenIsland(UUID islandId, String generatorName) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        try {
            String query = UPSERT_GENERATOR_ISLAND.formatted(this.databaseName);
            MariaDBExecute.executeQueryDML(MariaDBInit.getPool(), query, List.of(islandId, generatorName),
                    i -> completableFuture.complete(i != 0), null);
        } catch (DatabaseException exception) {
            log.error(exception.getMessage(), exception);
            completableFuture.complete(false);
        }
        return completableFuture;
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
