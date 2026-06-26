package fr.euphyllia.skyllia.listeners.bukkitevents.player;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.event.players.PlayerRespawnSkylliaEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.UUID;

public class RespawnEvent implements Listener {

    private final InterneAPI api;

    public RespawnEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        if (event.isBedSpawn() || event.isAnchorSpawn()) return;
        World world = event.getPlayer().getWorld();
        if (!SkylliaAPI.isWorldSkyblock(world)) return;
        Location resolved = resolveRespawnLocation(event.getPlayer(), world);
        if (resolved != null) {
            event.setRespawnLocation(resolved);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRespawnSkylla(final PlayerRespawnSkylliaEvent event) {
        if (event.isCancelled()) return;
        World world = event.getPlayer().getWorld();
        if (!SkylliaAPI.isWorldSkyblock(world)) return;
        Location resolved = resolveRespawnLocation(event.getPlayer(), world);
        if (resolved != null) {
            event.setRespawnLocation(resolved);
        }
    }

    private Location resolveRespawnLocation(Player player, World islandWorld) {
        final UUID playerId = player.getUniqueId();
        final Island island = SkylliaAPI.getIslandByPlayerId(playerId);

        if (island != null) {
            Location spawn = island.getSpawnLocation(islandWorld);
            if (spawn != null && spawn.getWorld() != null) {
                return spawn.add(0, 0.5, 0);
            }
        }

        if (ConfigLoader.playerManager.isTeleportSpawnIfNoIsland()) {
            Location spawnLocation = ConfigLoader.general.getSpawnLocation();
            if (spawnLocation != null && spawnLocation.getWorld() != null) {
                return spawnLocation;
            }
        }

        return null;
    }
}