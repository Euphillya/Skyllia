package fr.euphyllia.skylliachallenge.listener;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import fr.euphyllia.skylliachallenge.challenge.Challenge;
import fr.euphyllia.skylliachallenge.hook.HookManager;
import fr.euphyllia.skylliachallenge.requirement.BlockBreakRequirement;
import fr.euphyllia.skylliachallenge.storage.ProgressStoragePartial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.UUID;

public class BlockRequirementListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        final Block block = event.getBlock();
        final UUID playerId = event.getPlayer().getUniqueId();
        final Location location = block.getLocation();
        final int chunkX = location.getBlockX() >> 4;
        final int chunkZ = location.getBlockZ() >> 4;

        final BlockData state = block.getBlockData();
        final Material material = block.getType();
        if (!material.equals(Material.SUGAR_CANE) && !material.equals(Material.CACTUS) && !material.equals(Material.BAMBOO)) {
            if (state instanceof Ageable ageable) {
                if (ageable.getAge() < ageable.getMaximumAge()) {
                    return;
                }
            }
        }

        Bukkit.getAsyncScheduler().runNow(SkylliaChallenge.getInstance(), task -> {

            Island islandAtLocation = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
            if (islandAtLocation == null) return;

            if (SkylliaChallenge.getInstance().isMustBeOnPlayerIsland()) {
                Island playerIsland = SkylliaAPI.getIslandByPlayerId(playerId);
                if (playerIsland == null) return;
                if (!islandAtLocation.getId().equals(playerIsland.getId())) return;
            }

            for (Challenge challenge : SkylliaChallenge.getInstance().getChallengeManager().getChallenges()) {
                if (challenge.getRequirements() == null) continue;
                for (ChallengeRequirement req : challenge.getRequirements()) {
                    if (req instanceof BlockBreakRequirement bbr) {
                        if (!matchesBlock(bbr, block, material)) continue;
                        ProgressStoragePartial.addPartial(islandAtLocation.getId(), challenge.getId(), bbr.requirementId(), 1);
                    }
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        final Block block = event.getBlock();
        final Location location = block.getLocation();
        final UUID playerId = event.getPlayer().getUniqueId();
        final int chunkX = location.getBlockX() >> 4;
        final int chunkZ = location.getBlockZ() >> 4;
        final BlockData state = block.getBlockData();
        final Material material = block.getType();
        if (state instanceof Ageable) return; // On ne pénalise pas les placements de cultures.

        Bukkit.getAsyncScheduler().runNow(SkylliaChallenge.getInstance(), task -> {
            Island islandAtLocation = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
            if (islandAtLocation == null) return;

            if (SkylliaChallenge.getInstance().isMustBeOnPlayerIsland()) {
                Island playerIsland = SkylliaAPI.getIslandByPlayerId(playerId);
                if (playerIsland == null) return;
                if (!islandAtLocation.getId().equals(playerIsland.getId())) return;
            }

            for (Challenge challenge : SkylliaChallenge.getInstance().getChallengeManager().getChallenges()) {
                if (challenge.getRequirements() == null) continue;
                for (ChallengeRequirement req : challenge.getRequirements()) {
                    if (req instanceof BlockBreakRequirement bbr) {
                        if (!matchesBlock(bbr, block, material)) continue;
                        ProgressStoragePartial.addPartial(islandAtLocation.getId(), challenge.getId(), bbr.requirementId(), -1);
                    }
                }
            }
        });
    }

    /**
     * Match d'un bloc contre une requirement : custom si la requirement référence Nexo/Oraxen,
     * sinon comparaison vanilla du {@link Material}.
     */
    private boolean matchesBlock(BlockBreakRequirement bbr, Block block, Material material) {
        if (bbr.isCustom()) {
            return HookManager.matchesBlock(block, bbr.customNamespace(), bbr.customId());
        }
        return bbr.getMaterial() != null && bbr.getMaterial().equals(material);
    }
}
