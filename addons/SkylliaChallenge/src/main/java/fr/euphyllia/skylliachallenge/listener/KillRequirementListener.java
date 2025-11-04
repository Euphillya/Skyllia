package fr.euphyllia.skylliachallenge.listener;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import fr.euphyllia.skylliachallenge.challenge.Challenge;
import fr.euphyllia.skylliachallenge.requirement.KillEntityRequirement;
import fr.euphyllia.skylliachallenge.storage.ProgressStoragePartial;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class KillRequirementListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeathEvent(EntityDeathEvent event) {

        final Entity entity = event.getEntity();
        final Player player = event.getEntity().getKiller();
        final EntityType entityType = entity.getType();
        if (player == null) return;

        Bukkit.getAsyncScheduler().runNow(SkylliaChallenge.getInstance(), task -> {
            Island island = SkylliaAPI.getCacheIslandByPlayerId(player.getUniqueId());
            if (island == null) return;

            for (Challenge challenge : SkylliaChallenge.getInstance().getChallengeManager().getChallenges()) {
                if (challenge.getRequirements() == null) continue;
                for (ChallengeRequirement req : challenge.getRequirements()) {
                    if (req instanceof KillEntityRequirement ker) {
                        if (!ker.entityType().equals(entityType)) continue;
                        ProgressStoragePartial.addPartial(island.getId(), challenge.getId(), ker.requirementId(), 1);
                    }
                }
            }
        });
    }

}