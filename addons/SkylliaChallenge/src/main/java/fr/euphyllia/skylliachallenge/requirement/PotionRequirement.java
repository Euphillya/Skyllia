package fr.euphyllia.skylliachallenge.requirement;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.util.Locale;
import java.util.Map;

/**
 * @param count how many active effects of this type (usually 1)
 */
public record PotionRequirement(int requirementId, NamespacedKey challengeKey, PotionType potionType, int data, int count) implements ChallengeRequirement {

    @Override
    public boolean isMet(Player player, Island island) {
        long counted = player.getActivePotionEffects().stream()
                .map(PotionEffect::getType)
                .filter(type -> type.getKey().value().equalsIgnoreCase(potionType.getKey().value()))
                .count();
        return counted >= count;
    }

    @Override
    public Component getDisplay(Locale locale) {
        return ConfigLoader.language.translate(locale, "addons.challenge.requirement.potion.display", Map.of(
                "%potion_name%", potionType.name(),
                "%potion_data%", String.valueOf(data),
                "%amount%", String.valueOf(count)
        ), false);
    }
}