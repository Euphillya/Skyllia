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

public class PlayerConsumeRequirementListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerConsume(final PlayerItemConsumeEvent event) {

        final Player player = event.getPlayer();
        final Material material = event.getItem().getType();

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
                    if (req instanceof PlayerConsumeRequirement ker) {
                        if (!ker.getMaterial().equals(material)) continue;
                        ProgressStoragePartial.addPartial(playerIsland.getId(), challenge.getId(), ker.requirementId(), 1);
                    }
                }
            }
        });
    }
}
