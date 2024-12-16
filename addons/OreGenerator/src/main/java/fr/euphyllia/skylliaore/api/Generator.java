package fr.euphyllia.skylliaore.api;

import java.util.List;
import java.util.Map;

public record Generator(String name, List<String> replaceBlocks, List<String> worlds,
                        Map<String, Double> blockChances) {

}