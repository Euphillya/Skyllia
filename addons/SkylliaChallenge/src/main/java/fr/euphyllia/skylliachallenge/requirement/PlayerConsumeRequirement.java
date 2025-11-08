package fr.euphyllia.skylliachallenge.requirement;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import fr.euphyllia.skylliachallenge.storage.ProgressStoragePartial;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;

public record PlayerConsumeRequirement(int requirementId, NamespacedKey challengeKey, String material,
                                       int count) implements ChallengeRequirement {
    private static final Logger log = LoggerFactory.getLogger(PlayerConsumeRequirement.class);

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
        return collected >= count;
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
        String displayMaterial = material().startsWith("potion[") ? parsePotion() : this.material;
        return ConfigLoader.language.translate(locale, "addons.challenge.requirement.player_consume.display", Map.of(
                "%amount%", String.valueOf(count),
                "%material%", displayMaterial
        ), false);
    }

    public String getMaterial() {
        return material;
    }

    public boolean isPotionRequirement() {
        return material.startsWith("potion[");
    }

    public String parsePotion() {
        // Retourne au format: potion[type=INSTANT_HEALTH,level=2]
        if (material.startsWith("potion[")) {
            String content = material.substring(7, material.length() - 1); // entre les []
            String[] parts = content.split(",");
            String type = "";
            String level = "1";
            for (String part : parts) {
                String[] kv = part.split("=");
                if (kv.length != 2) continue;
                String key = kv[0].trim();
                String val = kv[1].trim();

                if (key.equalsIgnoreCase("type")) {
                    type = val.toUpperCase(Locale.ROOT);
                } else if (key.equalsIgnoreCase("level")) {
                    level = val;
                }
            }
            return "potion[type=" + type + ",level=" + level + "]";
        }
        return "";
    }

    public boolean isPotion(String potionConfig, String potionConsume) {
        String normalizedConfig = potionConfig.startsWith("potion[") ? potionConfig : parsePotion();
        return normalizedConfig.equalsIgnoreCase(potionConsume);
    }
}

