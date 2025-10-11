package fr.euphyllia.skylliachallenge.requirement;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

/**
 * @param amount combien d’effets actifs du type (souvent 1)
 */
public record PotionRequirement(PotionType potionType, int data, int amount) implements ChallengeRequirement {

    @Override
    public boolean isMet(Player player, Island island) {
        long count = player.getActivePotionEffects().stream()
                .map(PotionEffect::getType)
                .filter(type -> type.getKey().value().equalsIgnoreCase(potionType.getKey().value()))
                .count();
        return count >= amount;
    }

    @Override
    public String getDisplay() {
        return "Avoir l’effet " + potionType.name() + " (variante " + data + ") x" + amount;
    }
}