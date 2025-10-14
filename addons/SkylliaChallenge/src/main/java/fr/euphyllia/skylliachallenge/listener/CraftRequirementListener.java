package fr.euphyllia.skylliachallenge.listener;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import fr.euphyllia.skylliachallenge.challenge.Challenge;
import fr.euphyllia.skylliachallenge.requirement.CraftRequirement;
import fr.euphyllia.skylliachallenge.storage.ProgressStoragePartial;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class CraftRequirementListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Island island = SkylliaAPI.getCacheIslandByPlayerId(player.getUniqueId());
        if (island == null) return;

        ItemStack result = event.getCurrentItem();
        if (result == null) return;
        for (Challenge challenge : SkylliaChallenge.getInstance().getChallengeManager().getChallenges()) {
            if (challenge.getRequirements() == null) continue;
            for (ChallengeRequirement req : challenge.getRequirements()) {
                if (req instanceof CraftRequirement cr) {
                    if (cr.material() != result.getType()) continue;
                    if (result.hasItemMeta()) {
                        var meta = result.getItemMeta();
                        if (cr.itemModel() != null) {
                            if (!CraftRequirement.HAS_ITEM_MODEL_METHOD) continue;
                            var key = meta.getItemModel();
                            if (key == null || !key.equals(cr.itemModel())) continue;
                        } else if (cr.customModelData() != -1) {
                            if (!meta.hasCustomModelData() || meta.getCustomModelData() != cr.customModelData())
                                continue;
                        }
                    }
                    ProgressStoragePartial.addPartial(island.getId(), challenge.getId(), cr.requirementId(), result.getAmount());
                }
            }
        }
    }
}
