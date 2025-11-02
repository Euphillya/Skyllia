package fr.euphyllia.skylliachallenge.requirement;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import fr.euphyllia.skylliachallenge.storage.ProgressStoragePartial;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;
import java.util.Map;

public record CraftRequirement(int requirementId, NamespacedKey challengeKey, Material material, int count,
                               String itemName,
                               int customModelData, NamespacedKey itemModel) implements ChallengeRequirement {

    public static final boolean HAS_ITEM_MODEL_METHOD;

    static {
        boolean hasMethod;
        try {
            ItemMeta.class.getMethod("getItemModel");
            hasMethod = true;
        } catch (NoSuchMethodException e) {
            hasMethod = false;
        }
        HAS_ITEM_MODEL_METHOD = hasMethod;
    }

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
     * @param locale the locale to use for translation
     * @return a short displayable string
     */
    @Override
    public Component getDisplay(Locale locale) {
        return ConfigLoader.language.translate(locale, "addons.challenge.requirement.craft.display", Map.of(
                "%item_name%", itemName,
                "%amount%", String.valueOf(count)
        ), false);
    }
}
