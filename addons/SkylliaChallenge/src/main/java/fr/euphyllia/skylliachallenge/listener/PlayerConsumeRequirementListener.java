package fr.euphyllia.skylliachallenge.listener;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import fr.euphyllia.skylliachallenge.challenge.Challenge;
import fr.euphyllia.skylliachallenge.requirement.PlayerConsumeRequirement;
import fr.euphyllia.skylliachallenge.storage.ProgressStoragePartial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlayerConsumeRequirementListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerConsume(final PlayerItemConsumeEvent event) {

        final Player player = event.getPlayer();
        final ItemStack itemStack = event.getItem();

        final Location location = player.getLocation();
        final int chunkX = location.getBlockX() >> 4;
        final int chunkZ = location.getBlockZ() >> 4;
        List<String> potionsConsumed = getPotionsConsumed(itemStack);

        Bukkit.getAsyncScheduler().runNow(SkylliaChallenge.getInstance(), task -> {
            Island playerIsland = SkylliaAPI.getCacheIslandByPlayerId(player.getUniqueId());
            if (playerIsland == null) return;

            Island islandAtLocation = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
            if (islandAtLocation == null) return;
            if (!islandAtLocation.getId().equals(playerIsland.getId())) return;

            for (Challenge challenge : SkylliaChallenge.getInstance().getChallengeManager().getChallenges()) {
                if (challenge.getRequirements() == null) continue;
                for (ChallengeRequirement req : challenge.getRequirements()) {
                    if (req instanceof PlayerConsumeRequirement ker) {
                        if (ker.isPotionRequirement()) {
                            for (String potionConsumed : potionsConsumed) {
                                if (ker.isPotion(ker.parsePotion(),  potionConsumed)) {
                                    ProgressStoragePartial.addPartial(playerIsland.getId(), challenge.getId(), ker.requirementId(), 1);
                                }
                            }
                            return;
                        }
                        ProgressStoragePartial.addPartial(playerIsland.getId(), challenge.getId(), ker.requirementId(), 1);
                    }
                }
            }
        });
    }

    private static @NotNull List<String> getPotionsConsumed(ItemStack itemStack) {
        List<String> potionsConsumed = new ArrayList<>();

        if (itemStack.getType() == Material.POTION) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta instanceof PotionMeta potionMeta) {
                List<PotionEffect> potionEffects = potionMeta.getAllEffects();
                for (PotionEffect effect : potionEffects) {
                    // Parse comme ceci : potion[type=HEAL,level=2]
                    String consumedPotion = "potion[type=" + effect.getType().getName().toUpperCase() + ",level=" + (effect.getAmplifier() + 1) + "]";
                    potionsConsumed.add(consumedPotion);
                }
            }
        }
        return potionsConsumed;
    }


}
