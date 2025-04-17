package fr.euphyllia.skyllia_papi.hook;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliaore.SkylliaOre;
import fr.euphyllia.skylliaore.api.Generator;

import java.util.UUID;

/**
 * Handles placeholders related to the SkylliaOre addon.
 */
public class OrePlaceHolder {
    /**
     * Processes ore-related placeholders.
     *
     * @param island      the player's island
     * @param playerId    the player's UUID
     * @param placeholder the placeholder to process
     * @return the placeholder value as a string
     */
    public static String processPlaceholder(Island island, UUID playerId, String placeholder) {
        Generator generator = SkylliaOre.getCache().getGeneratorIsland(island.getId());
        return generator.name();
    }
}
