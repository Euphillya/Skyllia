package fr.euphyllia.skylliachallenge.database.postgre;

import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import fr.euphyllia.skylliachallenge.api.database.ProgressBackend;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class PostgresProgressBackend implements ProgressBackend {

    private final DatabaseLoader db;

    public PostgresProgressBackend(DatabaseLoader db) {
        this.db = db;
    }

    @Override
    public void preloadProgress(Consumer<ProgressRow> sink) {
        SQLExecute.queryMap(db,
                "SELECT island_id, challenge_id, times_completed, last_completed_at FROM island_challenge_progress;",
                null,
                rs -> {
                    try {
                        while (rs != null && rs.next()) {
                            sink.accept(new ProgressBackend.ProgressRow(
                                    UUID.fromString(rs.getString("island_id")),
                                    rs.getString("challenge_id"),
                                    rs.getInt("times_completed"),
                                    rs.getLong("last_completed_at")
                            ));
                        }
                    } catch (SQLException ignored) {
                    }
                    return null;
                });
    }

    @Override
    public void preloadPartial(Consumer<PartialRow> sink) {
        SQLExecute.queryMap(db,
                "SELECT island_id, challenge_id, requirement_id, collected_amount FROM island_challenge_partial;",
                null,
                rs -> {
                    try {
                        while (rs != null && rs.next()) {
                            sink.accept(new PartialRow(
                                    UUID.fromString(rs.getString("island_id")),
                                    rs.getString("challenge_id"),
                                    rs.getInt("requirement_id"),
                                    rs.getLong("collected_amount")
                            ));
                        }
                    } catch (SQLException ignored) {
                    }
                    return null;
                });
    }

    @Override
    public void upsertProgressSet(UUID islandId, String challengeId, int timesCompleted, long lastCompletedAt) {
        SQLExecute.update(db, """
                INSERT INTO island_challenge_progress (island_id, challenge_id, times_completed, last_completed_at)
                VALUES(?, ?, ?, ?)
                ON CONFLICT(island_id, challenge_id) DO UPDATE SET
                    times_completed = EXCLUDED.times_completed,
                    last_completed_at = GREATEST(EXCLUDED.last_completed_at, island_challenge_progress.last_completed_at);
                """, List.of(islandId, challengeId, timesCompleted, lastCompletedAt));
    }

    @Override
    public void incrementCompletion(UUID islandId, String challengeId, long nowEpochMillis) {
        SQLExecute.update(db, """
                INSERT INTO island_challenge_progress (island_id, challenge_id, times_completed, last_completed_at)
                VALUES(?, ?, 1, ?)
                ON CONFLICT(island_id, challenge_id)
                DO UPDATE SET
                    times_completed = island_challenge_progress.times_completed + 1,
                    last_completed_at = EXCLUDED.last_completed_at;
                """, List.of(islandId, challengeId, nowEpochMillis));
    }

    @Override
    public void setPartial(UUID islandId, String challengeId, int requirementId, long collectedAmount) {
        SQLExecute.update(db, """
                INSERT INTO island_challenge_partial (island_id, challenge_id, requirement_id, collected_amount)
                VALUES(?, ?, ?, ?)
                ON CONFLICT(island_id, challenge_id, requirement_id)
                DO UPDATE SET collected_amount = EXCLUDED.collected_amount;
                """, List.of(islandId, challengeId, requirementId, collectedAmount));
    }

    @Override
    public void deletePartialForChallenge(UUID islandId, String challengeId) {
        SQLExecute.update(db, """
                DELETE FROM island_challenge_partial
                WHERE island_id = ? AND challenge_id = ?;
                """, List.of(islandId, challengeId));
    }
}