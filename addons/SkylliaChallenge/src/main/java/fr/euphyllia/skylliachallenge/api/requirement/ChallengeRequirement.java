package fr.euphyllia.skylliachallenge.api.requirement;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.entity.Player;

public interface ChallengeRequirement {

    boolean isMet(Player player, Island island);

    String getDisplay();
}
