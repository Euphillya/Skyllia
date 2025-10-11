package fr.euphyllia.skylliachallenge.managers;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import fr.euphyllia.skylliachallenge.api.reward.ChallengeReward;
import fr.euphyllia.skylliachallenge.challenge.Challenge;
import fr.euphyllia.skylliachallenge.gui.ChallengeGui;
import fr.euphyllia.skylliachallenge.loader.ChallengeYamlLoader;
import fr.euphyllia.skylliachallenge.storage.ProgressStorage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all registered {@link Challenge} objects in the plugin.
 * <p>
 * Its responsibilities include:
 * <ul>
 *     <li>Loading challenge definitions from YAML files</li>
 *     <li>Providing runtime access to challenges by ID</li>
 *     <li>Evaluating completion conditions for a player and island</li>
 *     <li>Applying rewards and updating persistent progress</li>
 *     <li>Opening the GUI to display available challenges</li>
 * </ul>
 *
 * <p>
 * This class acts as the central controller for everything related to challenge
 * validation and progression.
 */
public class ChallengeManagers {

    private final SkylliaChallenge skylliaChallenge;
    private final Map<NamespacedKey, Challenge> challengeMap = new ConcurrentHashMap<>();

    /**
     * Creates a new manager bound to the plugin instance.
     *
     * @param challenge the plugin instance
     */
    public ChallengeManagers(SkylliaChallenge challenge) {
        this.skylliaChallenge = challenge;
    }

    /** @return all currently registered challenges */
    public Collection<Challenge> getChallenges() {
        return challengeMap.values();
    }

    /**
     * Retrieves a specific challenge by its unique key.
     *
     * @param key the challenge ID
     * @return the corresponding challenge, or {@code null} if none is found
     */
    @Nullable
    public Challenge getChallenge(NamespacedKey key) {
        return challengeMap.get(key);
    }

    /**
     * Registers a new challenge in memory.
     */
    public void registerChallenge(Challenge challenge) {
        challengeMap.put(challenge.getId(), challenge);
    }

    /**
     * Removes a challenge from memory.
     */
    public void unregisterChallenge(NamespacedKey key) {
        challengeMap.remove(key);
    }

    /**
     * Clears all loaded challenges (used before reloads).
     */
    public void clearChallenges() {
        challengeMap.clear();
    }

    /**
     * Loads challenge definitions from a filesystem folder
     * using {@link ChallengeYamlLoader}, overwriting any existing ones.
     *
     * @param folder the directory containing challenge YAMLs
     */
    public void loadChallenges(File folder) {
        clearChallenges();
        ChallengeYamlLoader.loadFolder(skylliaChallenge, folder).forEach(this::registerChallenge);
    }

    /**
     * Checks if a player is currently eligible to complete a challenge.
     *
     * This verifies:
     * <ul>
     *     <li>Global completion limit ({@link Challenge#getMaxTimes()})</li>
     *     <li>All {@link ChallengeRequirement}s return {@code true}</li>
     * </ul>
     */
    public boolean canComplete(Island island, Challenge challenge, Player actor) {
        if (challenge.getMaxTimes() >= 0) {
            int times = ProgressStorage.getTimesCompleted(island.getId(), challenge.getId());
            if (times >= challenge.getMaxTimes()) {
                return false;
            }
        }
        if (challenge.getRequirements() != null) {
            for (ChallengeRequirement requirement : challenge.getRequirements()) {
                if (!requirement.isMet(actor, island)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Attempts to complete a challenge for the given island and player.
     * <p>
     * The process is:
     * <ol>
     *     <li>Check completion limit</li>
     *     <li>Consume resources via {@link ChallengeRequirement#consume}</li>
     *     <li>Re-evaluate {@link ChallengeRequirement#isMet} to verify</li>
     *     <li>Apply rewards and increment {@link ProgressStorage}</li>
     *     <li>Broadcast if configured</li>
     * </ol>
     *
     * @return {@code true} if completion was successful
     */
    public boolean complete(Island island, Challenge challenge, Player actor) {
        if (challenge.getMaxTimes() >= 0) {
            int times = ProgressStorage.getTimesCompleted(island.getId(), challenge.getId());
            if (times >= challenge.getMaxTimes()) return false;
        }

        if (challenge.getRequirements() != null) {
            for (ChallengeRequirement req : challenge.getRequirements()) {
                if (!req.consume(actor, island)) {
                    return false;
                }
            }
        }

        boolean allMet = true;
        if (challenge.getRequirements() != null) {
            for (ChallengeRequirement req : challenge.getRequirements()) {
                if (!req.isMet(actor, island)) {
                    allMet = false;
                    break;
                }
            }
        }

        if (!allMet) {
            return false;
        }

        ProgressStorage.increment(island.getId(), challenge.getId());

        if (challenge.getRewards() != null) {
            for (ChallengeReward reward : challenge.getRewards()) {
                reward.apply(actor, island);
            }
        }

        if (challenge.isBroadcastCompletion()) {
            Bukkit.broadcast(ConfigLoader.language.translate(actor, "addons.challenge.player.notify-complete", Map.of(
                    "player_name", actor.getName(),
                    "challenge_name", challenge.getName()
            )), "skyllia.challenge.notify");
        }

        return true;
    }

    /**
     * Opens the challenge GUI for a player.
     */
    public void openGui(Player player) {
        new ChallengeGui(skylliaChallenge, this).open(player);
    }
}
