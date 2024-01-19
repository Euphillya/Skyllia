package fr.euphyllia.skyllia.cache;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
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
        positionIslandId.put(position, island);
    }
}
