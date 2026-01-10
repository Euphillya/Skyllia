package fr.euphyllia.skylliachallenge.listener;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import fr.euphyllia.skylliachallenge.challenge.Challenge;
import fr.euphyllia.skylliachallenge.requirement.CraftRequirement;
import fr.euphyllia.skylliachallenge.storage.ProgressStoragePartial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CraftRequirementListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(final CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getAction().equals(InventoryAction.NOTHING)) {
            return;
        }
        final ItemStack result = getCraftedItem(event);
        if (result.getType().isAir()) return;
        final Material material = result.getType();
        final ItemMeta meta = result.getItemMeta();
        NamespacedKey model;
        Integer customModelData;
        if (CraftRequirement.HAS_ITEM_MODEL_METHOD) {
            customModelData = null;
            model = meta.getItemModel();
        } else {
            model = null;
            if (meta.hasCustomModelData()) {
                customModelData = meta.getCustomModelData();
            } else {
                customModelData = null;
            }
        }

        final Location location = player.getLocation();
        final int chunkX = location.getBlockX() >> 4;
        final int chunkZ = location.getBlockZ() >> 4;

        Bukkit.getAsyncScheduler().runNow(SkylliaChallenge.getInstance(), task -> {
            // On travaille toujours sur l'île du joueur
            Island playerIsland = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
            if (playerIsland == null) return;

            // Si activé en config, on vérifie que le joueur est bien sur SON île
            if (SkylliaChallenge.getInstance().isMustBeOnPlayerIsland()) {
                Island islandAtLocation = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
                if (islandAtLocation == null) return;
                if (!islandAtLocation.getId().equals(playerIsland.getId())) return;
            }

            for (Challenge challenge : SkylliaChallenge.getInstance().getChallengeManager().getChallenges()) {
                if (challenge.getRequirements() == null) continue;
                for (ChallengeRequirement req : challenge.getRequirements()) {
                    if (req instanceof CraftRequirement cr) {
                        if (cr.material() != material) continue;
                        if (result.hasItemMeta()) {

                            if (cr.itemModel() != null) {
                                if (!CraftRequirement.HAS_ITEM_MODEL_METHOD) continue;
                                if (model == null || !model.equals(cr.itemModel())) continue;
                            } else if (cr.customModelData() != -1) {
                                if (customModelData == null || !customModelData.equals(cr.customModelData())) {
                                    continue;
                                }
                            }
                        }
                        ProgressStoragePartial.addPartial(
                                playerIsland.getId(),
                                challenge.getId(),
                                cr.requirementId(),
                                result.getAmount()
                        );
                    }
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    private ItemStack getCraftedItem(final CraftItemEvent event) {
        if (event.isShiftClick()) {
            final ItemStack recipeResult = event.getRecipe().getResult();
            final int resultAmt = recipeResult.getAmount();
            int leastIngredient = -1;
            for (final ItemStack item : event.getInventory().getMatrix()) {
                if (item != null && !item.getType().equals(Material.AIR)) {
                    final int re = item.getAmount() * resultAmt;
                    if (leastIngredient == -1 || re < leastIngredient) {
                        leastIngredient = re;
                    }
                }
            }
            return new ItemStack(recipeResult.getType(), leastIngredient, recipeResult.getDurability());
        }
        return event.getCurrentItem();
    }
}
