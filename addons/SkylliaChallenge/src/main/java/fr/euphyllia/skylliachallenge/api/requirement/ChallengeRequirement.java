package fr.euphyllia.skylliachallenge.api.requirement;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.entity.Player;

/**
 * Represents a single requirement needed to complete a {@code Challenge}.
 * <p>
 * Typical implementations may include collecting items, having enough money,
 * reaching a certain island level, or any other custom condition.
 * </p>
 *
 * <p>Each requirement has two main responsibilities:</p>
 * <ul>
 *     <li><b>Validation</b> via {@link #isMet(Player, Island)} — to determine if the requirement is fulfilled.</li>
 *     <li><b>Consumption</b> via {@link #consume(Player, Island)} — to optionally remove resources
 *         (items, money, etc.) when the player attempts to validate the challenge.</li>
 * </ul>
 *
 * <p>
 * Example implementations:
 * <ul>
 *     <li>ItemRequirement → checks & removes specific items from inventory</li>
 *     <li>BankRequirement → checks & withdraws currency from island bank</li>
 * </ul>
 * </p>
 */
public interface ChallengeRequirement {

    /**
     * Checks whether this requirement is currently fulfilled by the given player and island.
     *
     * @param player the player attempting the challenge (never {@code null})
     * @param island the island associated with the challenge (never {@code null})
     * @return {@code true} if the requirement is met and ready to be validated
     */
    boolean isMet(Player player, Island island);

    /**
     * Optionally consumes the necessary resources to satisfy this requirement.
     * <p>
     * This method is called when the player clicks <i>Validate</i> in the GUI.
     * It should remove or deduct the required amount of items, money or other resources.
     * </p>
     *
     * <p>
     * Default behavior does nothing and simply returns {@code true}.
     * Implementations may return {@code false} if the deduction fails.
     * </p>
     *
     * @param player the player who is attempting the challenge
     * @param island the island associated with the challenge
     * @return {@code true} if resources were correctly consumed or not needed
     */
    default boolean consume(Player player, Island island) {
        return true;
    }

    /**
     * Returns a human-readable description of this requirement.
     * <p>
     * Used in GUIs and lore displays to inform the player about what is needed.
     * For example: {@code "Avoir 64 Blé"} or {@code "Posséder 5000$ en banque"}.
     * </p>
     *
     * @return a short displayable string
     */
    String getDisplay();
}
