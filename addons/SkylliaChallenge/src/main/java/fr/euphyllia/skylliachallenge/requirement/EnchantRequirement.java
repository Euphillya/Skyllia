package fr.euphyllia.skylliachallenge.requirement;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import fr.euphyllia.skylliachallenge.storage.ProgressStoragePartial;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;

public record EnchantRequirement(int requirementId, NamespacedKey challengeKey, Enchantment enchantment, int level, int amount,
                                 boolean strict) implements ChallengeRequirement {
    /**
     * Checks whether this requirement is currently fulfilled by the given player and island.
     *
     * @param player the player attempting the challenge (never {@code null})
     * @param island the island associated with the challenge (never {@code null})
     * @return {@code true} if the requirement is met and ready to be validated
     */
    @Override
    public boolean isMet(Player player, Island island) {
        long collected = ProgressStoragePartial.getPartial(island.getId(), challengeKey, requirementId);
        return collected >= amount;
    }

    /**
     * Returns a human-readable description of this requirement.
     * <p>
     * Used in GUIs and lore displays to inform the player about what is needed.
     * For example: {@code "Avoir 64 Blé"} or {@code "Posséder 5000$ en banque"}.
     * </p>
     *
     * @param locale
     * @return a short displayable string
     */
    @Override
    public Component getDisplay(Locale locale) {
        return ConfigLoader.language.translate(locale, "addons.challenge.requirement.player_consume.display", Map.of(
                "%enchantment%", enchantment.getKey().getKey(),
                "%level%", String.valueOf(level)
        ), false);
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    public int getLevel() {
        return level;
    }

    /*
     * Strict veut dire uniquement cet enchantement uniquement au level spécifié
     */
    public boolean isStrict() {
        return strict;
    }
}
