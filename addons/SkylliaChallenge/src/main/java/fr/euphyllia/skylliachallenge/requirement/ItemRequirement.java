package fr.euphyllia.skylliachallenge.requirement;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import fr.euphyllia.skylliachallenge.storage.ProgressStoragePartial;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @param customModelData Avant 1.21.4, -1 si pas utilisé
 * @param itemModel       Depuis 1.21.4
 */
public record ItemRequirement(int requirementId, NamespacedKey challengeKey, Material material, int count,
                              String itemName, int customModelData,
                              NamespacedKey itemModel) implements ChallengeRequirement {

    @Override
    public boolean isMet(Player player, Island island) {
        int have = 0;
        for (ItemStack is : player.getInventory().getContents()) {
            if (is == null) continue;
            if (is.getType() != material) continue;

            ItemMeta meta = is.getItemMeta();
            if (meta == null) continue;

            if (itemModel != null) {
                try {
                    NamespacedKey key = meta.getItemModel();
                    if (key == null || !key.equals(itemModel)) continue;
                } catch (NoSuchMethodError e) {
                    // Version de Bukkit trop ancienne
                    return false;
                }
            } else if (customModelData != -1) {
                if (!meta.hasCustomModelData() || meta.getCustomModelData() != customModelData) continue;
            }
            have += is.getAmount();
            if (have >= count) return true;
        }
        return have >= count;
    }

    @Override
    public void consume(Player player, Island island) {
        long already = ProgressStoragePartial.getPartial(island.getId(), challengeKey, requirementId);
        if (already >= count) return;
        long needed = count - already;
        long deposited = 0;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack is = contents[i];
            if (is == null) continue;
            if (is.getType() != material) continue;
            ItemMeta meta = is.getItemMeta();
            if (meta == null) continue;

            if (itemModel != null) {
                try {
                    NamespacedKey key = meta.getItemModel();
                    if (key == null || !key.equals(itemModel)) continue;
                } catch (NoSuchMethodError e) {
                    // Version de Bukkit trop ancienne
                    return;
                }
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
            player.getInventory().setContents(contents);
            ProgressStoragePartial.addPartial(island.getId(), challengeKey, requirementId, deposited);
        }
    }

    @Override
    public String getDisplay() {
        return "Avoir " + count + " " + itemName;
    }
}
