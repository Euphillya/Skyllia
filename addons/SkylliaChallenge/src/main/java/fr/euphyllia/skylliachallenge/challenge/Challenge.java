package fr.euphyllia.skylliachallenge.challenge;

import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import fr.euphyllia.skylliachallenge.api.reward.ChallengeReward;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Represents a challenge that can be completed by an Island.
 * <p>
 * Each challenge defines:
 * <ul>
 *     <li>a unique identifier ({@link NamespacedKey})</li>
 *     <li>a display name and lore for UI purposes</li>
 *     <li>one or more {@link ChallengeRequirement} to validate</li>
 *     <li>one or more {@link ChallengeReward} to grant upon completion</li>
 *     <li>optionally, how many times it may be completed and whether it should be broadcast</li>
 *     <li>GUI settings for how it is displayed in menus</li>
 * </ul>
 * <p>
 * Instances are usually created via configuration loaders such as {@code ChallengeYamlLoader}
 * and then consumed by {@code ChallengeManagers}.
 */
public class Challenge {

    private final NamespacedKey id;
    private String name;
    private List<Component> lore;
    private List<ChallengeRequirement> requirements;
    private List<ChallengeReward> rewards;

    /**
     * Maximum number of times an Island can complete this challenge.
     * <p>
     * Use {@code -1} to indicate no limit (infinite completions).
     */
    private int maxTimes;

    /**
     * Whether a server-wide broadcast should be sent when this challenge is completed.
     */
    private boolean broadcastCompletion;

    /**
     * Whether this challenge should be shown in the GUI.
     */
    private boolean showInGUI;

    /**
     * The base {@link ItemStack} used as the visual representation of the challenge in the GUI.
     */
    private ItemStack guiItem;

    /**
     * The displayed amount for the GUI item.
     */
    private int guiItemAmount;

    /**
     * Additional lore lines displayed only inside the GUI.
     */
    private List<Component> guiLore;

    private PositionGUI positionGUI;

    /**
     * Creates a new challenge with the given unique identifier.
     *
     * @param id a non-null {@link NamespacedKey} acting as the unique ID of the challenge
     */
    public Challenge(NamespacedKey id) {
        this.id = id;
    }

    /**
     * @return the unique identifier of this challenge
     */
    public NamespacedKey getId() {
        return id;
    }

    /**
     * @return the display name of this challenge
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of this challenge.
     */
    public Challenge setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @return the lore description shown outside of GUI context (e.g. requirements or explanation)
     */
    public List<Component> getLore() {
        return lore;
    }

    /**
     * Sets the main lore description of this challenge.
     */
    public Challenge setLore(List<Component> lore) {
        this.lore = lore;
        return this;
    }

    /**
     * @return the list of requirements that must be met to complete this challenge
     */
    public List<ChallengeRequirement> getRequirements() {
        return requirements;
    }

    /**
     * Sets the list of requirements to validate.
     */
    public Challenge setRequirements(List<ChallengeRequirement> requirements) {
        this.requirements = requirements;
        return this;
    }

    /**
     * @return the list of rewards granted when this challenge is completed
     */
    public List<ChallengeReward> getRewards() {
        return rewards;
    }

    /**
     * Sets the list of rewards to apply on completion.
     */
    public Challenge setRewards(List<ChallengeReward> rewards) {
        this.rewards = rewards;
        return this;
    }

    /**
     * @return the maximum completion count allowed, or {@code -1} for infinite
     */
    public int getMaxTimes() {
        return maxTimes;
    }

    /**
     * Sets the max number of completions allowed (use -1 for infinite).
     */
    public Challenge setMaxTimes(int maxTimes) {
        this.maxTimes = maxTimes;
        return this;
    }

    /**
     * @return {@code true} if a broadcast should be sent upon challenge completion
     */
    public boolean isBroadcastCompletion() {
        return broadcastCompletion;
    }

    /**
     * Enables or disables the broadcast upon completion.
     */
    public Challenge setBroadcastCompletion(boolean broadcastCompletion) {
        this.broadcastCompletion = broadcastCompletion;
        return this;
    }

    /**
     * @return {@code true} if this challenge should be displayed in GUI menus
     */
    public boolean isShowInGUI() {
        return showInGUI;
    }

    /**
     * Sets whether this challenge is visible in GUI menus.
     */
    public Challenge setShowInGUI(boolean showInGUI) {
        this.showInGUI = showInGUI;
        return this;
    }

    /**
     * @return the {@link ItemStack} used as GUI icon
     */
    public ItemStack getGuiItem() {
        return guiItem;
    }

    /**
     * Sets the {@link ItemStack} used as GUI icon.
     */
    public Challenge setGuiItem(ItemStack guiItem) {
        this.guiItem = guiItem;
        return this;
    }

    /**
     * @return the displayed amount on the GUI item
     */
    public int getGuiItemAmount() {
        return guiItemAmount;
    }

    /**
     * Sets the displayed amount for the GUI item.
     */
    public Challenge setGuiItemAmount(int guiItemAmount) {
        this.guiItemAmount = guiItemAmount;
        return this;
    }

    /**
     * @return additional lore displayed only in GUI context
     */
    public List<Component> getGuiLore() {
        return guiLore;
    }

    /**
     * Sets the additional GUI-only lore.
     */
    public Challenge setGuiLore(List<Component> guiLore) {
        this.guiLore = guiLore;
        return this;
    }

    public PositionGUI getPositionGUI() {
        return positionGUI;
    }

    public Challenge setPositionGUI(PositionGUI positionGUI) {
        this.positionGUI = positionGUI;
        return this;
    }

    public record PositionGUI(int page, int row, int column) {
    }
}
