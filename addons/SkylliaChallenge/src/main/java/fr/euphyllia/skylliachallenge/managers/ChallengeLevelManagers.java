package fr.euphyllia.skylliachallenge.managers;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import fr.euphyllia.skylliachallenge.api.reward.ChallengeReward;
import fr.euphyllia.skylliachallenge.challenge.Challenge;
import fr.euphyllia.skylliachallenge.challenge.ChallengeLevel;
import fr.euphyllia.skylliachallenge.gui.ChallengeLevelGui;
import fr.euphyllia.skylliachallenge.loader.ChallengeLevelYamlLoader;
import fr.euphyllia.skylliachallenge.storage.ProgressStorage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all registered {@link ChallengeLevel} tiers.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Loading level definitions from YAML</li>
 *     <li>Resolving the unlock state of a level for a given island (root level or predecessor completed)</li>
 *     <li>Detecting when a level becomes completed (enough of its challenges done, within the skip allowance)</li>
 *     <li>Granting level rewards and unlocking the next level upon completion</li>
 * </ul>
 *
 * <p>Level completion is persisted by reusing {@link ProgressStorage}: a completed level is recorded
 * with its own {@link NamespacedKey} (so {@code getTimesCompleted(level) > 0} means "completed").
 * No additional database schema is required.</p>
 */
public class ChallengeLevelManagers {

    private final SkylliaChallenge plugin;
    private final Map<NamespacedKey, ChallengeLevel> levelMap = new ConcurrentHashMap<>();

    /**
     * Ids that are referenced as a {@code next-level} by another level. Any level whose id is NOT in
     * this set is considered a "root" level and is unlocked from the start.
     */
    private final Set<NamespacedKey> referencedAsNext = ConcurrentHashMap.newKeySet();

    public ChallengeLevelManagers(SkylliaChallenge plugin) {
        this.plugin = plugin;
    }

    public Collection<ChallengeLevel> getLevels() {
        return levelMap.values();
    }

    @Nullable
    public ChallengeLevel getLevel(NamespacedKey key) {
        return levelMap.get(key);
    }

    public void registerLevel(ChallengeLevel level) {
        levelMap.put(level.getId(), level);
        if (level.getNextLevel() != null) {
            referencedAsNext.add(level.getNextLevel());
        }
    }

    public void clearLevels() {
        levelMap.clear();
        referencedAsNext.clear();
    }

    public void loadLevels(File folder) {
        clearLevels();
        ChallengeLevelYamlLoader.loadFolder(plugin, folder).forEach(this::registerLevel);
    }

    /**
     * @return {@code true} if the island has already completed the given level.
     */
    public boolean isCompleted(Island island, ChallengeLevel level) {
        return ProgressStorage.getTimesCompleted(island.getId(), level.getId()) > 0;
    }

    /**
     * @return {@code true} if the level is unlocked for the island (it is a root level, or the level
     * whose {@code next-level} points to it has been completed).
     */
    public boolean isUnlocked(Island island, ChallengeLevel level) {
        if (!referencedAsNext.contains(level.getId())) {
            return true; // root level
        }
        for (ChallengeLevel other : levelMap.values()) {
            if (level.getId().equals(other.getNextLevel())) {
                if (isCompleted(island, other)) return true;
            }
        }
        return false;
    }

    /**
     * @return how many of the level's challenges the island has completed at least once.
     */
    public int countCompletedChallenges(Island island, ChallengeLevel level) {
        if (level.getChallenges() == null) return 0;
        int done = 0;
        for (NamespacedKey challengeId : level.getChallenges()) {
            if (ProgressStorage.getTimesCompleted(island.getId(), challengeId) > 0) {
                done++;
            }
        }
        return done;
    }

    /**
     * @return the number of challenges that must be completed for the level to count as finished
     * (total minus the skip allowance, never below zero).
     */
    public int getRequiredCount(ChallengeLevel level) {
        int total = level.getChallenges() == null ? 0 : level.getChallenges().size();
        return Math.max(0, total - level.getMaxSkip());
    }

    public boolean meetsCompletion(Island island, ChallengeLevel level) {
        return countCompletedChallenges(island, level) >= getRequiredCount(level);
    }

    /**
     * Evaluates every level for the island and completes any newly-finished, unlocked level.
     * <p>
     * Should be called after a challenge is completed (and when the level GUI is opened) so rewards
     * are granted and the next level unlocked as soon as the conditions are met.
     * </p>
     */
    public void evaluate(Island island, Player actor) {
        boolean changed = true;
        Set<NamespacedKey> handled = new HashSet<>();
        // Loop because completing a level can unlock another that is already satisfied.
        while (changed) {
            changed = false;
            for (ChallengeLevel level : levelMap.values()) {
                if (handled.contains(level.getId())) continue;
                if (isCompleted(island, level)) {
                    handled.add(level.getId());
                    continue;
                }
                if (!isUnlocked(island, level)) continue;
                if (!meetsCompletion(island, level)) continue;

                completeLevel(island, actor, level);
                handled.add(level.getId());
                changed = true;
            }
        }
    }

    private void completeLevel(Island island, Player actor, ChallengeLevel level) {
        ProgressStorage.updateCompletion(island.getId(), level.getId(), System.currentTimeMillis());

        if (level.getRewards() != null && !level.getRewards().isEmpty()) {
            // Proxy challenge so rewards relying on a Challenge (e.g. %challenge_name%) keep working.
            Challenge proxy = new Challenge(level.getId()).setName(level.getName());
            for (ChallengeReward reward : level.getRewards()) {
                reward.apply(actor, island, proxy);
            }
        }

        if (level.isBroadcastCompletion()) {
            Bukkit.broadcast(ConfigLoader.language.translate(actor, "addons.challenge.level.notify-complete", Map.of(
                    "%player_name%", actor.getName(),
                    "%level_name%", level.getName()
            )), "skyllia.challenge.notify");
        }

        ConfigLoader.language.sendMessage(actor, "addons.challenge.level.complete", Map.of(
                "%level_name%", level.getName()
        ));
    }

    public void openGui(Player player) {
        new ChallengeLevelGui(plugin, this).open(player, 1);
    }
}
