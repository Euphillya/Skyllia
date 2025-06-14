package fr.euphyllia.skyllia.listeners.bukkitevents.gamerule.entity;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.skyblock.model.gamerule.GameRuleIsland;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.concurrent.CopyOnWriteArrayList;

public class ExplosionEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(ExplosionEvent.class);
    CopyOnWriteArrayList<EntityType> explosionByHumanEntity;
    CopyOnWriteArrayList<EntityType> explosionByMobEntity;

    public ExplosionEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
        explosionByHumanEntity = new CopyOnWriteArrayList<>();
        //explosionByHumanEntity.add(getEntityType("TNT", EntityType.valueOf("PRIMED_TNT"))); Todo ? 1.21.4 not supported...
        //explosionByHumanEntity.add(getEntityType("TNT_MINECART", EntityType.valueOf("MINECART_TNT")));

        explosionByMobEntity = new CopyOnWriteArrayList<>();
        explosionByMobEntity.add(EntityType.CREEPER);
        explosionByMobEntity.add(EntityType.GHAST);
        explosionByMobEntity.add(EntityType.WITHER);
        explosionByMobEntity.add(EntityType.WITHER_SKULL);
    }

    private static EntityType getEntityType(String name, EntityType defaultType) {
        try {
            return EntityType.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return defaultType;
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onExplode(final EntityExplodeEvent event) {
        EntityType causeExplosion = event.getEntityType();
        Location location = event.getLocation();
        if (explosionByHumanEntity.contains(causeExplosion)) {
            ListenersUtils.checkGameRuleIsland(location, GameRuleIsland.DISABLE_HUMAN_EXPLOSION, event);
        } else if (explosionByMobEntity.contains(causeExplosion)) {
            ListenersUtils.checkGameRuleIsland(location, GameRuleIsland.DISABLE_MOB_EXPLOSION, event);
        } else {
            ListenersUtils.checkGameRuleIsland(location, GameRuleIsland.DISABLE_UNKNOWN_EXPLOSION, event);
        }
    }
}