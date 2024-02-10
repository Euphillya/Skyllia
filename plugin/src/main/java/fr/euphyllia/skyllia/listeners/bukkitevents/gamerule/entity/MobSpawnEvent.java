package fr.euphyllia.skyllia.listeners.bukkitevents.gamerule.entity;

import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.skyblock.model.gamerule.GameRuleIsland;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import fr.euphyllia.skyllia.utils.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.concurrent.CopyOnWriteArrayList;

public class MobSpawnEvent implements Listener {
    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(MobSpawnEvent.class);
    CopyOnWriteArrayList<CreatureSpawnEvent.SpawnReason> spawnReasonIgnore = new CopyOnWriteArrayList<>();

    public MobSpawnEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
        this.spawnReasonIgnore.add(CreatureSpawnEvent.SpawnReason.SPAWNER);
        this.spawnReasonIgnore.add(CreatureSpawnEvent.SpawnReason.EGG);
        this.spawnReasonIgnore.add(CreatureSpawnEvent.SpawnReason.COMMAND);
        this.spawnReasonIgnore.add(CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPreCreatureSpawn(final PreCreatureSpawnEvent event) {
        if (event.isCancelled()) return;
        if (this.spawnReasonIgnore.contains(event.getReason())) return;
        EntityType entityType = event.getType();
        Location location = event.getSpawnLocation();
        if (EntityUtils.isMonster(entityType)) {
            ListenersUtils.checkGameRuleIsland(location, GameRuleIsland.DISABLE_SPAWN_HOSTILE, event);
        } else if (EntityUtils.isPassif(entityType)) {
            ListenersUtils.checkGameRuleIsland(location, GameRuleIsland.DISABLE_SPAWN_PASSIVE, event);
        } else {
            ListenersUtils.checkGameRuleIsland(location, GameRuleIsland.DISABLE_SPAWN_UNKNOWN, event);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCreatureSpawn(final CreatureSpawnEvent event) {
        if (event.isCancelled()) return;
        if (this.spawnReasonIgnore.contains(event.getSpawnReason())) return;
        Entity entity = event.getEntity();
        Location location = event.getLocation();
        if (entity instanceof Monster) {
            ListenersUtils.checkGameRuleIsland(location, GameRuleIsland.DISABLE_SPAWN_HOSTILE, event);
        } else if (entity instanceof Animals || entity instanceof Ambient) {
            ListenersUtils.checkGameRuleIsland(location, GameRuleIsland.DISABLE_SPAWN_PASSIVE, event);
        } else {
            ListenersUtils.checkGameRuleIsland(location, GameRuleIsland.DISABLE_SPAWN_UNKNOWN, event);
        }
    }
}
