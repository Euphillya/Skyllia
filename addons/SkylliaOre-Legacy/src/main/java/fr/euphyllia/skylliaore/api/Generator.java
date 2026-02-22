package fr.euphyllia.skylliaore.api;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record Generator(String name, Set<String> replaceBlocks, Set<String> worlds,
                        Map<String, Double> blockChances) {

    public Generator(String name, List<String> replaceBlocks, List<String> worlds,
                     Map<String, Double> blockChances) {
        this(
                name,
                replaceBlocks.stream().map(String::toLowerCase).collect(Collectors.toUnmodifiableSet()),
                worlds.stream().map(String::toLowerCase).collect(Collectors.toUnmodifiableSet()),
                Map.copyOf(blockChances)
        );
    }
}
