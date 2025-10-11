package fr.euphyllia.skylliachallenge.api.reward;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.entity.Player;

public interface ChallengeReward {

    void apply(Player player, Island island);

    String getDisplay();
}