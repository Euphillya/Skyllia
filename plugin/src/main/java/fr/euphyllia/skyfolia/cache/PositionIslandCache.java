package fr.euphyllia.skyfolia.cache;

import fr.euphyllia.skyfolia.api.skyblock.model.Position;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PositionIslandCache {

    private static final ConcurrentHashMap<String, UUID> positionIslandId = new ConcurrentHashMap<>();

    public @Nullable UUID getIslandId(Position position) {
        return positionIslandId.getOrDefault(position.toString(), null);
    }

    public static ConcurrentMap<String, UUID> getPositionIslandId() {
        return positionIslandId;
    }
}
