package fr.euphyllia.skylliachallenge.listener;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import fr.euphyllia.skylliachallenge.challenge.Challenge;
import fr.euphyllia.skylliachallenge.requirement.EnchantRequirement;
import fr.euphyllia.skylliachallenge.storage.ProgressStoragePartial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class PlayerEnchantRequirementListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerEnchant(final EnchantItemEvent event) {
        final Player player = event.getEnchanter();
        final @NotNull Map<Enchantment, Integer> enchants = event.getEnchantsToAdd();

        final Location location = player.getLocation();
        final int chunkX = location.getBlockX() >> 4;
        final int chunkZ = location.getBlockZ() >> 4;
        final UUID uuid = player.getUniqueId();

        Bukkit.getAsyncScheduler().runNow(SkylliaChallenge.getInstance(), task -> {
            Island playerIsland = SkylliaAPI.getIslandByPlayerId(uuid);
            if (playerIsland == null) return;

            if (SkylliaChallenge.getInstance().isMustBeOnPlayerIsland()) {
                Island islandAtLocation = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
                if (islandAtLocation == null) return;
                if (!islandAtLocation.getId().equals(playerIsland.getId())) return;
            }

            for (Challenge challenge : SkylliaChallenge.getInstance().getChallengeManager().getChallenges()) {
                if (challenge.getRequirements() == null) continue;
                for (ChallengeRequirement req : challenge.getRequirements()) {
                    if (req instanceof EnchantRequirement er) {
                        if (er.isStrict()) {
                            if (enchants.size() != 1) continue;
                        }
                        if (!enchants.containsKey(er.enchantment())) continue;
                        if (!enchants.get(er.enchantment()).equals(er.level())) continue;

                        ProgressStoragePartial.addPartial(
                                playerIsland.getId(),
                                challenge.getId(),
                                er.requirementId(),
                                1
                        );
                    }
                }
            }
        });
    }
}
