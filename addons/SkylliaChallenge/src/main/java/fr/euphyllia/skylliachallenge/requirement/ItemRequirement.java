package fr.euphyllia.skylliachallenge.requirement;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import fr.euphyllia.skylliachallenge.storage.ProgressStoragePartial;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;
import java.util.Map;

/**
 * @param customModelData Avant 1.21.4, -1 si pas utilisé
 * @param itemModel       Depuis 1.21.4
 */
public record ItemRequirement(int requirementId, NamespacedKey challengeKey, Material material, int count,
                              String itemName, int customModelData,
                              NamespacedKey itemModel) implements ChallengeRequirement {

    private static final boolean HAS_ITEM_MODEL_METHOD;

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

    @Override
    public boolean isMet(Player player, Island island) {
        long already = ProgressStoragePartial.getPartial(island.getId(), challengeKey, requirementId);
        int have = 0;
        for (ItemStack is : player.getInventory().getContents()) {
            if (is == null) continue;
            if (is.getType() != material) continue;

            ItemMeta meta = is.getItemMeta();
            if (meta == null) continue;

            if (itemModel != null) {
                if (!HAS_ITEM_MODEL_METHOD) return false;
                NamespacedKey key = meta.getItemModel();
                if (key == null || !key.equals(itemModel)) continue;
            } else if (customModelData != -1) {
                if (!meta.hasCustomModelData() || meta.getCustomModelData() != customModelData) continue;
            }
            have += is.getAmount();
        }
        return (already + have) >= count;
    }

    @Override
    public boolean consume(Player player, Island island) {
        long already = ProgressStoragePartial.getPartial(island.getId(), challengeKey, requirementId);
        long needed = count - already;
        if (needed <= 0) return true;

        long deposited = 0;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack is = contents[i];
            if (is == null) continue;
            if (is.getType() != material) continue;
            ItemMeta meta = is.getItemMeta();
            if (meta == null) continue;

            if (itemModel != null) {
                if (!HAS_ITEM_MODEL_METHOD) return false;
                NamespacedKey key = meta.getItemModel();
                if (key == null || !key.equals(itemModel)) continue;
            } else if (customModelData != -1) {
                if (!meta.hasCustomModelData() || meta.getCustomModelData() != customModelData) continue;
            }

            int take = (int) Math.min(is.getAmount(), needed - deposited);
            if (take <= 0) continue;

            is.setAmount(is.getAmount() - take);
            if (is.getAmount() <= 0) contents[i] = null;
            deposited += take;
        }

        if (deposited > 0) {
            ProgressStoragePartial.addPartial(island.getId(), challengeKey, requirementId, deposited);
            player.getInventory().setContents(contents);
        }

        return deposited == needed;
    }

    @Override
    public Component getDisplay(Locale locale) {
        return ConfigLoader.language.translate(locale, "addons.challenge.requirement.item.display", Map.of(
                "%item_name%", itemName,
                "%amount%", String.valueOf(count)
        ), false);
    }
}
