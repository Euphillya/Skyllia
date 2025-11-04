package fr.euphyllia.skylliachallenge.reward;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliachallenge.api.reward.ChallengeReward;
import fr.euphyllia.skylliachallenge.challenge.Challenge;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public record ItemReward(Material item, int count) implements ChallengeReward {

    @Override
    public void apply(Player player, Island island, Challenge challenge) {
        player.getInventory().addItem(new ItemStack(item, count));
    }

    @Override
    public Component getDisplay(Locale locale) {
        return null;
    }
}
