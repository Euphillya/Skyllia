package fr.euphyllia.skyfolia.cache;

import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.model.Position;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

public class PositionIslandCache {

    private static final ConcurrentHashMap<Position, Island> positionIslandId = new ConcurrentHashMap<>();

    public static void delete(Position position) {
        positionIslandId.remove(position);
    }

    public static @Nullable Island getIsland(Position position) {
        return positionIslandId.getOrDefault(position, null);
    }

    public static void add(Position position, Island island) {
        System.out.println("put");
        positionIslandId.put(position, island);
    }
}
