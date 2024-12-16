package fr.euphyllia.skyllia_papi;

import fr.euphyllia.skyllia.api.skyblock.Island;

import java.util.UUID;

public class OrePlaceHolder {
    public static String processOrePlaceholder(Island island, UUID playerId, String placeholder) {
        fr.euphyllia.skylliaore.api.Generator generator = fr.euphyllia.skylliaore.Main.getCache().getGeneratorIsland(island.getId());
        return generator.name();
    }
}
