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

public class ChallengeManagers {

    private final SkylliaChallenge skylliaChallenge;
    private final Map<NamespacedKey, Challenge> challengeMap = new ConcurrentHashMap<>();

    public ChallengeManagers(SkylliaChallenge challenge) {
        this.skylliaChallenge = challenge;
    }

    public Collection<Challenge> getChallenges() {
        return challengeMap.values();
    }

    @Nullable
    public Challenge getChallenge(NamespacedKey key) {
        return challengeMap.get(key);
    }

    public void registerChallenge(Challenge challenge) {
        challengeMap.put(challenge.getId(), challenge);
    }

    public void unregisterChallenge(NamespacedKey key) {
        challengeMap.remove(key);
    }

    public void clearChallenges() {
        challengeMap.clear();
    }

    public void loadChallenges(File folder) {
        clearChallenges(); // Clear existing challenges before loading new ones
        ChallengeYamlLoader.loadFolder(skylliaChallenge, folder).forEach(this::registerChallenge);
    }

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

    public void openGui(Player player) {
        new ChallengeGui(skylliaChallenge, this).open(player);
    }
}
