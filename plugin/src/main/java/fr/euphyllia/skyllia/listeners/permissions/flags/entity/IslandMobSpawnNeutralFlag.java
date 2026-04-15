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
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;

import java.util.EnumMap;
import java.util.Map;

public class IslandMobSpawnNeutralFlag implements FlagModule {

    private FlagId ALLOW_SPAWN_ALL_NEUTRAL;
    private Map<EntityType, FlagId> flagByType;

    @Override
    public void registerFlags(IslandFlagRegistry registry, Plugin owner) {
        this.ALLOW_SPAWN_ALL_NEUTRAL = registry.idOrRegister(new FlagNode(
                new NamespacedKey(owner, "island.spawn.neutral.all"),
                "island.flag.spawn_neutral_all.name",
                "island.flag.spawn_neutral_all.description"
        ));

        this.flagByType = new EnumMap<>(EntityType.class);
        Map<EntityType, String> supported = SkylliaAPI.getMobsSpawnImpl().supportedNeutralMobs();
        for (Map.Entry<EntityType, String> entry : supported.entrySet()) {
            flagByType.put(entry.getKey(), registry.idOrRegister(new FlagNode(
                    new NamespacedKey(owner, "island.spawn.neutral." + entry.getValue()),
                    "island.flag.spawn_neutral_" + entry.getValue() + ".name",
                    "island.flag.spawn_neutral_" + entry.getValue() + ".description"
            )));
        }
    }

    private void handleSpawn(EntityType type, Location location, Runnable cancel) {
        if (flagByType == null) return;
        FlagId specific = flagByType.get(type);
        if (specific == null) return;
        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;
        Island island = SkylliaAPI.getIslandByChunk(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        if (island == null) return;
        if (!SkylliaAPI.getPermissionsManager().hasFlag(island, specific, ALLOW_SPAWN_ALL_NEUTRAL)) {
            cancel.run();
            return;
        }
        if (ListenersUtils.isBlockOutsideIsland(island, location, null)) {
            cancel.run();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPreCreatureSpawn(final PreCreatureSpawnEvent event) {
        if (SkylliaAPI.getMobsSpawnImpl().ignoredReasons().contains(event.getReason())) return;
        handleSpawn(event.getType(), event.getSpawnLocation(), () -> {
            event.setCancelled(true);
            event.setShouldAbortSpawn(true);
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onCreatureSpawn(final CreatureSpawnEvent event) {
        if (SkylliaAPI.getMobsSpawnImpl().ignoredReasons().contains(event.getSpawnReason())) return;
        handleSpawn(event.getEntityType(), event.getLocation(), () -> event.setCancelled(true));
    }
}