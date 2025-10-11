package fr.euphyllia.skylliachallenge.challenge;

import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import fr.euphyllia.skylliachallenge.api.reward.ChallengeReward;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Challenge {

    private final NamespacedKey id;
    private String name;
    private List<Component> lore;
    private List<ChallengeRequirement> requirements;
    private List<ChallengeReward> rewards;

    private int maxTimes; // -1 for infinite
    private boolean broadcastCompletion;

    private boolean showInGUI;
    private int slot;
    private ItemStack guiItem;
    private int guiItemAmount;
    private List<Component> guiLore;

    public Challenge(NamespacedKey id) {
        this.id = id;
    }

    public NamespacedKey getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Challenge setName(String name) {
        this.name = name;
        return this;
    }

    public List<Component> getLore() {
        return lore;
    }

    public Challenge setLore(List<Component> lore) {
        this.lore = lore;
        return this;
    }

    public List<ChallengeRequirement> getRequirements() {
        return requirements;
    }

    public Challenge setRequirements(List<ChallengeRequirement> requirements) {
        this.requirements = requirements;
        return this;
    }

    public List<ChallengeReward> getRewards() {
        return rewards;
    }

    public Challenge setRewards(List<ChallengeReward> rewards) {
        this.rewards = rewards;
        return this;
    }

    public int getMaxTimes() {
        return maxTimes;
    }

    public Challenge setMaxTimes(int maxTimes) {
        this.maxTimes = maxTimes;
        return this;
    }

    public boolean isBroadcastCompletion() {
        return broadcastCompletion;
    }

    public Challenge setBroadcastCompletion(boolean broadcastCompletion) {
        this.broadcastCompletion = broadcastCompletion;
        return this;
    }

    public boolean isShowInGUI() {
        return showInGUI;
    }

    public Challenge setShowInGUI(boolean showInGUI) {
        this.showInGUI = showInGUI;
        return this;
    }

    public int getSlot() {
        return slot;
    }

    public Challenge setSlot(int slot) {
        this.slot = slot;
        return this;
    }

    public ItemStack getGuiItem() {
        return guiItem;
    }

    public Challenge setGuiItem(ItemStack guiItem) {
        this.guiItem = guiItem;
        return this;
    }

    public int getGuiItemAmount() {
        return guiItemAmount;
    }

    public Challenge setGuiItemAmount(int guiItemAmount) {
        this.guiItemAmount = guiItemAmount;
        return this;
    }

    public List<Component> getGuiLore() {
        return guiLore;
    }

    public Challenge setGuiLore(List<Component> guiLore) {
        this.guiLore = guiLore;
        return this;
    }
}
