package fr.euphyllia.skylliachallenge.reward;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliachallenge.api.reward.ChallengeReward;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public record ItemReward(Material item, int count) implements ChallengeReward {

    @Override
    public void apply(Player player, Island island) {
        player.getInventory().addItem(new ItemStack(item, count));
    }

    @Override
    public String getDisplay() {
        return "Récompense: " + count + "x " + item.name();
    }
}
