package fr.euphyllia.skylliaore;

import fr.euphyllia.skylliaore.api.OreGenerator;

import java.util.UUID;

public class GeneratorManager {

    private final OreGenerator dbGenerator;

    public GeneratorManager(OreGenerator dbGenerator) {
        this.dbGenerator = dbGenerator;
    }

    public Boolean updateGenerator(UUID islandId, String generatorId) {
        boolean ok = dbGenerator.updateGenIsland(islandId, generatorId);
        if (ok) {
            SkylliaOre.invalidateIslandCache(islandId);
            SkylliaOre.getInstance().getOreCache().refreshGeneratorAsync(
                    SkylliaOre.getInstance(),
                    islandId,
                    () -> SkylliaOre.getInstance().getOreGenerator().getGenIsland(islandId));
        }
        return ok;
    }
}
