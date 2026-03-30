package fr.euphyllia.skylliagenerator.api;

import org.bukkit.NamespacedKey;

import java.util.Map;
import java.util.Set;

public record GeneratorDefinition(
        NamespacedKey id,
        String displayName,
        GeneratorType type,
        Set<String> worlds,
        Map<String, Double> blockWeights
) {

    public static final GeneratorDefinition COBBLESTONE_FALLBACK;

    static {
        COBBLESTONE_FALLBACK = new GeneratorDefinition(
                new NamespacedKey("skylliagenerator", "default"),
                "Default",
                GeneratorType.COBBLESTONE,
                Set.of(),
                Map.of("COBBLESTONE", 100.0)
        );
    }
}
