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

public class IslandMobSpawnBossFlag implements FlagModule {

    private FlagId ALLOW_SPAWN_ALL_BOSS;
    private Map<EntityType, FlagId> flagByType;

    @Override
    public void registerFlags(IslandFlagRegistry registry, Plugin owner) {
        this.ALLOW_SPAWN_ALL_BOSS = registry.idOrRegister(new FlagNode(
                new NamespacedKey(owner, "island.spawn.boss.all"),
                "island.flag.spawn_boss_all.name",
                "island.flag.spawn_boss_all.description"
        ));

        this.flagByType = new EnumMap<>(EntityType.class);
        Map<EntityType, String> supported = SkylliaAPI.getMobsSpawnImpl().supportedBossMobs();
        for (Map.Entry<EntityType, String> entry : supported.entrySet()) {
            flagByType.put(entry.getKey(), registry.idOrRegister(new FlagNode(
                    new NamespacedKey(owner, "island.spawn.boss." + entry.getValue()),
                    "island.flag.spawn_boss_" + entry.getValue() + ".name",
                    "island.flag.spawn_boss_" + entry.getValue() + ".description"
            )));
        }
    }

    private void handleSpawn(EntityType type, Location location, Runnable cancel) {
        FlagId specific = flagByType.get(type);
        if (specific == null) return;
        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;
        Island island = SkylliaAPI.getIslandByChunk(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        if (island == null) return;
        if (!SkylliaAPI.getPermissionsManager().hasFlag(island, specific, ALLOW_SPAWN_ALL_BOSS)) {
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
