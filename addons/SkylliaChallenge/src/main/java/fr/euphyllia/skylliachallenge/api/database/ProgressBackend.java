package fr.euphyllia.skylliachallenge.api.database;

import java.util.UUID;
import java.util.function.Consumer;

public interface ProgressBackend {

    void preloadProgress(Consumer<ProgressRow> sink);

    void preloadPartial(Consumer<PartialRow> sink);

    void upsertProgressSet(UUID islandId, String challengeId, int timesCompleted, long lastCompletedAt);

    void incrementCompletion(UUID islandId, String challengeId, long nowEpochMillis);

    void setPartial(UUID islandId, String challengeId, int requirementId, long collectedAmount);

    void deletePartialForChallenge(UUID islandId, String challengeId);

    record ProgressRow(UUID islandId, String challengeId, int timesCompleted, long lastCompletedAt) {
    }

    record PartialRow(UUID islandId, String challengeId, int requirementId, long collectedAmount) {
    }
}
