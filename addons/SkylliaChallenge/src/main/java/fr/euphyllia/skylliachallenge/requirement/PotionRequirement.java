package fr.euphyllia.skylliachallenge.requirement;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.util.Locale;
import java.util.Map;

/**
 * @param amount how many active effects of this type (usually 1)
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
    public Component getDisplay(Locale locale) {
        return ConfigLoader.language.translate(locale, "addons.challenge.requirement.potion.display", Map.of(
                "%potion_name%", potionType.name(),
                "%potion_data%", String.valueOf(data),
                "%amount%", String.valueOf(amount)
        ), false);
    }
}