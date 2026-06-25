package fr.euphyllia.skylliachallenge.challenge;

import fr.euphyllia.skylliachallenge.api.reward.ChallengeReward;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a <b>challenge level</b> (a tier) that groups several {@link Challenge}s of a similar
 * difficulty together.
 * <p>
 * Challenge levels introduce progression: a level is considered completed once the island has
 * completed enough of the challenges it contains (allowing up to {@link #getMaxSkip()} of them to be
 * skipped). Completing a level can grant its own rewards and unlock the {@link #getNextLevel() next}
 * level, which can therefore act as a prerequisite gate for harder challenges.
 * </p>
 *
 * <p>Instances are built from configuration by {@code ChallengeLevelYamlLoader} and managed by
 * {@code ChallengeLevelManagers}.</p>
 */
public class ChallengeLevel {

    private final NamespacedKey id;
    private String name;
    private List<Component> lore;

    /**
     * The challenges that belong to this level.
     */
    private List<NamespacedKey> challenges;

    /**
     * Maximum number of challenges that may be left unfinished while still completing the level.
     * <p>
     * A value of {@code 0} means every challenge of the level must be completed.
     */
    private int maxSkip;

    /**
     * The level unlocked when this one is completed, or {@code null} if this is the last level.
     */
    private NamespacedKey nextLevel;

    private List<ChallengeReward> rewards;
    private boolean broadcastCompletion;
    private boolean showInGUI;

    private ItemStack guiItem;
    private ItemStack lockedItem;
    private int guiItemAmount;
    private List<Component> guiLore;
    private Challenge.PositionGUI positionGUI;

    public ChallengeLevel(NamespacedKey id) {
        this.id = id;
    }

    public NamespacedKey getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ChallengeLevel setName(String name) {
        this.name = name;
        return this;
    }

    public List<Component> getLore() {
        return lore;
    }

    public ChallengeLevel setLore(List<Component> lore) {
        this.lore = lore;
        return this;
    }

    public List<NamespacedKey> getChallenges() {
        return challenges;
    }

    public ChallengeLevel setChallenges(List<NamespacedKey> challenges) {
        this.challenges = challenges;
        return this;
    }

    public int getMaxSkip() {
        return maxSkip;
    }

    public ChallengeLevel setMaxSkip(int maxSkip) {
        this.maxSkip = maxSkip;
        return this;
    }

    public @Nullable NamespacedKey getNextLevel() {
        return nextLevel;
    }

    public ChallengeLevel setNextLevel(@Nullable NamespacedKey nextLevel) {
        this.nextLevel = nextLevel;
        return this;
    }

    public List<ChallengeReward> getRewards() {
        return rewards;
    }

    public ChallengeLevel setRewards(List<ChallengeReward> rewards) {
        this.rewards = rewards;
        return this;
    }

    public boolean isBroadcastCompletion() {
        return broadcastCompletion;
    }

    public ChallengeLevel setBroadcastCompletion(boolean broadcastCompletion) {
        this.broadcastCompletion = broadcastCompletion;
        return this;
    }

    public boolean isShowInGUI() {
        return showInGUI;
    }

    public ChallengeLevel setShowInGUI(boolean showInGUI) {
        this.showInGUI = showInGUI;
        return this;
    }

    public ItemStack getGuiItem() {
        return guiItem;
    }

    public ChallengeLevel setGuiItem(ItemStack guiItem) {
        this.guiItem = guiItem;
        return this;
    }

    /**
     * @return the item shown in the GUI while this level is still locked, or {@code null} to reuse
     * the normal {@link #getGuiItem()} icon.
     */
    public @Nullable ItemStack getLockedItem() {
        return lockedItem;
    }

    public ChallengeLevel setLockedItem(@Nullable ItemStack lockedItem) {
        this.lockedItem = lockedItem;
        return this;
    }

    public int getGuiItemAmount() {
        return guiItemAmount;
    }

    public ChallengeLevel setGuiItemAmount(int guiItemAmount) {
        this.guiItemAmount = guiItemAmount;
        return this;
    }

    public List<Component> getGuiLore() {
        return guiLore;
    }

    public ChallengeLevel setGuiLore(List<Component> guiLore) {
        this.guiLore = guiLore;
        return this;
    }

    public Challenge.PositionGUI getPositionGUI() {
        return positionGUI;
    }

    public ChallengeLevel setPositionGUI(Challenge.PositionGUI positionGUI) {
        this.positionGUI = positionGUI;
        return this;
    }
}
