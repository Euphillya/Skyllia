package fr.euphyllia.skylliachallenge.api.database;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Persistence layer for island challenge progress.
 * <p>
 * A {@code ProgressBackend} abstracts the underlying storage engine (SQLite, MariaDB, PostgreSQL, …)
 * behind a single contract, so the rest of the addon can read and write progress without knowing
 * which database is in use. Two kinds of data are persisted:
 * </p>
 * <ul>
 *     <li><b>Completion progress</b> — how many times an island has completed a given challenge and
 *         when it last did so (see {@link ProgressRow}).</li>
 *     <li><b>Partial progress</b> — the running counter of an in-flight requirement that accumulates
 *         over time, such as blocks broken or entities killed (see {@link PartialRow}).</li>
 * </ul>
 *
 * <p>
 * Implementations are expected to be thread-safe: write methods are typically invoked from
 * background executor threads, while the bulk {@code preload*} methods run once at startup to warm
 * the in-memory caches. Keys are identified by the island {@link UUID} together with the challenge
 * id (the string form of a {@code NamespacedKey}).
 * </p>
 */
public interface ProgressBackend {

    /**
     * Streams every stored completion row to the given consumer.
     * <p>
     * Called once during startup to load all challenge completions into memory. The {@code sink} is
     * invoked once per row; implementations should stream results rather than materialising the whole
     * table, as datasets may be large.
     * </p>
     *
     * @param sink callback invoked for each {@link ProgressRow} found in storage (never {@code null})
     */
    void preloadProgress(Consumer<ProgressRow> sink);

    /**
     * Streams every stored partial-progress row to the given consumer.
     * <p>
     * Called once during startup to load all in-flight requirement counters into memory. The
     * {@code sink} is invoked once per row.
     * </p>
     *
     * @param sink callback invoked for each {@link PartialRow} found in storage (never {@code null})
     */
    void preloadPartial(Consumer<PartialRow> sink);

    /**
     * Inserts or replaces the full completion row for an island/challenge pair.
     * <p>
     * Unlike {@link #incrementCompletion}, this writes an absolute value rather than applying a delta.
     * It is mainly used to flush the in-memory cache back to storage (for example on shutdown), where
     * the authoritative completion count and timestamp are already known.
     * </p>
     *
     * @param islandId        the island this row belongs to
     * @param challengeId     the challenge id (string form of its {@code NamespacedKey})
     * @param timesCompleted  the absolute number of completions to store
     * @param lastCompletedAt the epoch milliseconds of the last completion ({@code 0} if never)
     */
    void upsertProgressSet(UUID islandId, String challengeId, int timesCompleted, long lastCompletedAt);

    /**
     * Atomically records a single new completion for an island/challenge pair.
     * <p>
     * Increments the stored completion count by one (creating the row if needed) and updates the last
     * completion timestamp. This is the runtime write performed whenever an island completes a
     * challenge.
     * </p>
     *
     * @param islandId       the island that completed the challenge
     * @param challengeId    the challenge id (string form of its {@code NamespacedKey})
     * @param nowEpochMillis the completion time, in epoch milliseconds
     */
    void incrementCompletion(UUID islandId, String challengeId, long nowEpochMillis);

    /**
     * Inserts or replaces the partial-progress counter for a single requirement.
     * <p>
     * Each requirement of a challenge tracks its own accumulated amount (for example blocks broken so
     * far). This stores the absolute collected value for the given requirement, identified within the
     * challenge by its {@code requirementId}.
     * </p>
     *
     * @param islandId        the island this counter belongs to
     * @param challengeId     the challenge id (string form of its {@code NamespacedKey})
     * @param requirementId   the index of the requirement within the challenge
     * @param collectedAmount the absolute amount collected so far
     */
    void setPartial(UUID islandId, String challengeId, int requirementId, long collectedAmount);

    /**
     * Removes every partial-progress counter associated with a challenge for one island.
     * <p>
     * Typically called once the challenge is validated, so its requirement counters are reset before
     * the challenge can be attempted again.
     * </p>
     *
     * @param islandId    the island whose partial counters should be cleared
     * @param challengeId the challenge id (string form of its {@code NamespacedKey})
     */
    void deletePartialForChallenge(UUID islandId, String challengeId);

    /**
     * A single stored completion entry for an island/challenge pair.
     *
     * @param islandId        the island the progress belongs to
     * @param challengeId     the challenge id (string form of its {@code NamespacedKey})
     * @param timesCompleted  how many times the island has completed the challenge
     * @param lastCompletedAt epoch milliseconds of the last completion, or {@code 0} if never completed
     */
    record ProgressRow(UUID islandId, String challengeId, int timesCompleted, long lastCompletedAt) {
    }

    /**
     * A single stored partial-progress entry for one requirement of a challenge.
     *
     * @param islandId        the island the progress belongs to
     * @param challengeId     the challenge id (string form of its {@code NamespacedKey})
     * @param requirementId   the index of the requirement within the challenge
     * @param collectedAmount the amount accumulated so far for that requirement
     */
    record PartialRow(UUID islandId, String challengeId, int requirementId, long collectedAmount) {
    }
}