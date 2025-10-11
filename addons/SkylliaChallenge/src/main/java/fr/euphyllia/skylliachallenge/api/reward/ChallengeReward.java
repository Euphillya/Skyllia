package fr.euphyllia.skylliachallenge.api.reward;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * Represents a reward granted when a {@code Challenge} is successfully completed.
 * <p>
 * Typical reward implementations can include:
 * <ul>
 *     <li>Giving items to the player</li>
 *     <li>Executing console or player commands</li>
 *     <li>Adding money to the island bank</li>
 *     <li>Granting permissions, experience, or other bonuses</li>
 * </ul>
 *
 * <p>
 * Rewards are applied only after all requirements of the challenge have been met
 * and any {@link ChallengeRequirement#consume(Player, Island)}
 * operations have succeeded.
 * </p>
 */
public interface ChallengeReward {

    /**
     * Executes the reward logic for the given player and island.
     *
     * @param player the player receiving the reward (never {@code null})
     * @param island the island associated with the reward context (never {@code null})
     */
    void apply(Player player, Island island);

    /**
     * Returns a human-readable description of this reward.
     * <p>
     * Used in GUIs and lores to inform the player about what they will gain.
     * Examples:
     * <ul>
     *     <li>"x3 Diamants"</li>
     *     <li>"5000$ en banque"</li>
     *     <li>"Commande: /fly pendant 1h"</li>
     * </ul>
     * </p>
     *
     * @return a short displayable string describing this reward
     */
    Component getDisplay(Locale locale);
}
