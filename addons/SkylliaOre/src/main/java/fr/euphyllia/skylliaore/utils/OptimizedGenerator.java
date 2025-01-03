package fr.euphyllia.skylliaore.utils;

import fr.euphyllia.skylliaore.api.Generator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class OptimizedGenerator {
    private final Generator generator;
    private final List<BlockProbability> cumulativeProbabilities;
    private final double totalChance;

    public OptimizedGenerator(Generator generator) {
        this.generator = generator;
        this.cumulativeProbabilities = new ArrayList<>();
        double cumulative = 0.0;
        for (Map.Entry<String, Double> entry : generator.blockChances().entrySet()) {
            cumulative += entry.getValue();
            cumulativeProbabilities.add(new BlockProbability(entry.getKey(), cumulative));
        }
        this.cumulativeProbabilities.sort(Comparator.comparingDouble(BlockProbability::cumulativeChance));
        this.totalChance = cumulative;
    }

    public Generator getGenerator() {
        return generator;
    }

    public List<BlockProbability> getCumulativeProbabilities() {
        return cumulativeProbabilities;
    }

    public double getTotalChance() {
        return totalChance;
    }

    public record BlockProbability(String blockKey, double cumulativeChance) {}
}
