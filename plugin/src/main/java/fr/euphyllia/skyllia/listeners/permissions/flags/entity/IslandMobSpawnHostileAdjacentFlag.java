package fr.euphyllia.skyllia.listeners.permissions.flags.entity;

import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.FlagId;
import fr.euphyllia.skyllia.api.permissions.FlagNode;
import fr.euphyllia.skyllia.api.permissions.IslandFlagRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.FlagModule;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;

import java.util.EnumMap;
import java.util.Map;

public class IslandMobSpawnHostileAdjacentFlag implements FlagModule {

    private FlagId ALLOW_SPAWN_ALL_HOSTILE_ADJACENT;
    private Map<EntityType, FlagId> flagByType;

    @Override
    public void registerFlags(IslandFlagRegistry registry, Plugin owner) {
        this.ALLOW_SPAWN_ALL_HOSTILE_ADJACENT = registry.idOrRegister(new FlagNode(
                new NamespacedKey(owner, "island.spawn.hostile_adjacent.all"),
                "island.flag.spawn_hostile_adjacent_all.name",
                "island.flag.spawn_hostile_adjacent_all.description"
        ));

        this.flagByType = new EnumMap<>(EntityType.class);
        Map<EntityType, String> supported = SkylliaAPI.getMobsSpawnImpl().supportedHostileAdjacentMobs();
        for (Map.Entry<EntityType, String> entry : supported.entrySet()) {
            flagByType.put(entry.getKey(), registry.idOrRegister(new FlagNode(
                    new NamespacedKey(owner, "island.spawn.hostile_adjacent." + entry.getValue()),
                    "island.flag.spawn_hostile_adjacent_" + entry.getValue() + ".name",
                    "island.flag.spawn_hostile_adjacent_" + entry.getValue() + ".description"
            )));
        }
    }

    private boolean shouldCancelSpawn(EntityType type, Location location) {
        if (flagByType == null) return false;
        FlagId specific = flagByType.get(type);
        if (specific == null) return false;

        final World world = location.getWorld();
        final String worldName = world.getName();
        if (!SkylliaAPI.isWorldSkyblock(worldName)) return false;

        final int bx = location.getBlockX();
        final int by = location.getBlockY();
        final int bz = location.getBlockZ();

        final Island island = SkylliaAPI.getIslandByChunk(bx >> 4, bz >> 4);
        if (island == null) return false;

        if (!SkylliaAPI.getPermissionsManager().hasFlag(island, specific, ALLOW_SPAWN_ALL_HOSTILE_ADJACENT, worldName)) {
            return true;
        }
        return ListenersUtils.isBlockOutsideIsland(island, world, bx, by, bz, null);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPreCreatureSpawn(final PreCreatureSpawnEvent event) {
        if (SkylliaAPI.getMobsSpawnImpl().ignoredReasons().contains(event.getReason())) return;
        if (shouldCancelSpawn(event.getType(), event.getSpawnLocation())) {
            event.setCancelled(true);
            event.setShouldAbortSpawn(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onCreatureSpawn(final CreatureSpawnEvent event) {
        if (SkylliaAPI.getMobsSpawnImpl().ignoredReasons().contains(event.getSpawnReason())) return;
        if (shouldCancelSpawn(event.getEntityType(), event.getLocation())) {
            event.setCancelled(true);
        }
    }
}