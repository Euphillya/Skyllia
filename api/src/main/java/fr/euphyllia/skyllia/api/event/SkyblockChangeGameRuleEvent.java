package fr.euphyllia.skyllia.api.event;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event that is triggered when a game rule is changed on a Skyblock island.
 * <p>
 * This event is fired whenever a specific game rule on a Skyblock island is modified. It provides
 * access to the island where the change occurred and the new value of the game rule, allowing
 * plugins to perform actions in response to this modification.
 * </p>
 * <p>
 * To handle this event, plugins must register an event listener and implement the appropriate handler.
 * </p>
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * import fr.euphyllia.skyllia.api.event.SkyblockChangeGameRuleEvent;
 * import fr.euphyllia.skyllia.api.skyblock.Island;
 * import org.bukkit.event.EventHandler;
 * import org.bukkit.event.Listener;
 *
 * public class GameRuleChangeListener implements Listener {
 *
 *     /**
 *      * Handles the SkyblockChangeGameRuleEvent to monitor and respond to game rule changes.
 *      *
 *      * @param event The SkyblockChangeGameRuleEvent instance.
 *      *\/
 *     @EventHandler
 *     public void onSkyblockChangeGameRule(SkyblockChangeGameRuleEvent event) {
 *         Island island = event.getIsland();
 *         long newGameRuleValue = event.getGamerule();
 *
 *         // Example 1: Logging the Game Rule Change
 *         System.out.println("Game rule changed on island ID: " + island.getId()
 *             + " to new value: " + newGameRuleValue);
 *
 *         // Example 2: Performing Actions Based on Specific Game Rule Values
 *         if (newGameRuleValue == 1) {
 *             // Enable a special feature or reward for the island owner
 *             grantSpecialReward(island);
 *             island.getOwnerPlayer().sendMessage("A special feature has been enabled on your island!");
 *         } else if (newGameRuleValue == 0) {
 *             // Disable certain features or revoke rewards
 *             revokeSpecialReward(island);
 *             island.getOwnerPlayer().sendMessage("A special feature has been disabled on your island.");
 *         }
 *
 *         // Example 3: Validating the New Game Rule Value
 *         if (!isValidGameRule(newGameRuleValue)) {
 *             // Notify the owner about the invalid game rule change
 *             island.getOwnerPlayer().sendMessage("The new game rule value is invalid. Please choose a valid option.");
 *             // Optionally, revert the game rule to its previous value
 *             revertGameRuleChange(island);
 *         }
 *     }
 *
 *     /**
 *      * Grants a special reward to the island owner.
 *      *
 *      * @param island The island to grant the reward to.
 *      *\/
 *     private void grantSpecialReward(Island island) {
 *         // Implement reward logic here
 *         // For example, give the owner a special item or bonus
 *     }
 *
 *     /**
 *      * Revokes the special reward from the island owner.
 *      *
 *      * @param island The island to revoke the reward from.
 *      *\/
 *     private void revokeSpecialReward(Island island) {
 *         // Implement reward revocation logic here
 *         // For example, remove the special item or bonus from the owner
 *     }
 *
 *     /**
 *      * Validates whether the provided game rule value is acceptable.
 *      *
 *      * @param gameRuleValue The game rule value to validate.
 *      * @return {@code true} if the game rule value is valid, {@code false} otherwise.
 *      *\/
 *     private boolean isValidGameRule(long gameRuleValue) {
 *         // Implement validation logic here
 *         // For demonstration, let's assume valid values are 0 and 1
 *         return gameRuleValue == 0 || gameRuleValue == 1;
 *     }
 *
 *     /**
 *      * Reverts the game rule change to its previous value.
 *      *
 *      * @param island The island to revert the game rule change on.
 *      *\/
 *     private void revertGameRuleChange(Island island) {
 *         // Implement logic to revert the game rule change
 *         // This might involve storing the previous value and setting it back
 *     }
 * }
 * }</pre>
 *
 * @see Island
 */
public class SkyblockChangeGameRuleEvent extends Event {

    /**
     * The handler list for this event.
     */
    private static final HandlerList handlerList = new HandlerList();

    /**
     * The island where the game rule is being changed.
     */
    private final Island island;

    /**
     * The new value of the game rule.
     */
    private final long gamerule;

    /**
     * Constructs a new {@code SkyblockChangeGameRuleEvent}.
     *
     * @param island     The {@link Island} where the game rule is being changed.
     * @param gamerules  The new value of the game rule.
     */
    public SkyblockChangeGameRuleEvent(Island island, long gamerules) {
        super(true);
        this.island = island;
        this.gamerule = gamerules;
    }

    /**
     * Retrieves the handler list for this event.
     *
     * @return The {@link HandlerList} associated with this event.
     */
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    /**
     * Retrieves the handlers associated with this event.
     *
     * @return The {@link HandlerList} for this event.
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }

    /**
     * Retrieves the {@link Island} where the game rule is being changed.
     *
     * @return The {@link Island} involved in the game rule change.
     */
    public Island getIsland() {
        return this.island;
    }

    /**
     * Retrieves the new value of the game rule.
     *
     * @return The new game rule value as a {@code long}.
     */
    public long getGamerule() {
        return this.gamerule;
    }
}
