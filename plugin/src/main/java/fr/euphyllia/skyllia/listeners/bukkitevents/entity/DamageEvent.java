package fr.euphyllia.skyllia.listeners.bukkitevents.entity;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Monster;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(DamageEvent.class);

    public DamageEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamageEntity(final EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Player damager)) {
            return;
        }
        if (damager.hasPermission("skyllia.damage.entity.bypass")) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            ListenersUtils.checkPermission(event.getEntity().getChunk(), damager, PermissionsIsland.PVP, event);
        } else {
            if (event.getEntity() instanceof Monster) {
                ListenersUtils.checkPermission(event.getEntity().getChunk(), damager, PermissionsIsland.KILL_MONSTER, event);
            } else if (event.getEntity() instanceof Animals) {
                ListenersUtils.checkPermission(event.getEntity().getChunk(), damager, PermissionsIsland.KILL_ANIMAL, event);
            } else if (event.getEntity() instanceof NPC) {
                ListenersUtils.checkPermission(event.getEntity().getChunk(), damager, PermissionsIsland.KILL_NPC, event);
            } else {
                ListenersUtils.checkPermission(event.getEntity().getChunk(), damager, PermissionsIsland.KILL_UNKNOWN_ENTITY, event);
            }
        }
    }
}
