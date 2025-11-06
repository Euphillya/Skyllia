package fr.euphyllia.skylliachallenge.listener;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import fr.euphyllia.skylliachallenge.challenge.Challenge;
import fr.euphyllia.skylliachallenge.requirement.FishRequirement;
import fr.euphyllia.skylliachallenge.storage.ProgressStoragePartial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerFishRequirementListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(final PlayerFishEvent event) {
        final Player player = event.getPlayer();
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;

        final Item loot = (Item) event.getCaught();
        if (loot == null) return;
        if (loot.isDead()) return;
        final ItemStack itemStack = loot.getItemStack();
        final Material lootType = itemStack.getType();

        final Location location = player.getLocation();
        final int chunkX = location.getBlockX() >> 4;
        final int chunkZ = location.getBlockZ() >> 4;

        Bukkit.getAsyncScheduler().runNow(SkylliaChallenge.getInstance(), task -> {
            Island playerIsland = SkylliaAPI.getCacheIslandByPlayerId(player.getUniqueId());
            if (playerIsland == null) return;

            Island islandAtLocation = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
            if (islandAtLocation == null) return;
            if (!islandAtLocation.getId().equals(playerIsland.getId())) return;

            for (Challenge challenge : SkylliaChallenge.getInstance().getChallengeManager().getChallenges()) {
                if (challenge.getRequirements() == null) continue;
                for (ChallengeRequirement req : challenge.getRequirements()) {
                    if (req instanceof FishRequirement fr) {
                        if (!fr.entityType().equals(lootType)) continue;
                        ProgressStoragePartial.addPartial(playerIsland.getId(), challenge.getId(), fr.requirementId(), 1);
                    }
                }
            }
        });
    }
}
