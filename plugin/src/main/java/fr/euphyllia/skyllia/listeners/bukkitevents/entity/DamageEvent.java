package fr.euphyllia.skyllia.listeners.bukkitevents.entity;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.*;
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


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamageEntity(final EntityDamageByEntityEvent event) {
        Player damagerPlayer = null;

        if (event.getDamager() instanceof Player directPlayer) {
            damagerPlayer = directPlayer;
        } else if (event.getDamager() instanceof Arrow arrow) {
            if (arrow.getShooter() instanceof Player shooterPlayer) {
                damagerPlayer = shooterPlayer;
            }
        }

        if (damagerPlayer == null) {
            return;
        }

        if (PermissionImp.hasPermission(damagerPlayer, "skyllia.damage.entity.bypass")) {
            return;
        }

        Entity target = event.getEntity();
        if (target instanceof Player) {
            ListenersUtils.checkPermission(target.getLocation(), damagerPlayer, PermissionsIsland.PVP, event);
        } else if (target instanceof Monster) {
            ListenersUtils.checkPermission(target.getLocation(), damagerPlayer, PermissionsIsland.KILL_MONSTER, event);
        } else if (target instanceof Animals) {
            ListenersUtils.checkPermission(target.getLocation(), damagerPlayer, PermissionsIsland.KILL_ANIMAL, event);
        } else if (target instanceof NPC) {
            ListenersUtils.checkPermission(target.getLocation(), damagerPlayer, PermissionsIsland.KILL_NPC, event);
        } else if (target instanceof ItemFrame || target instanceof Painting) {
            ListenersUtils.checkPermission(target.getLocation(), damagerPlayer, PermissionsIsland.BLOCK_BREAK, event);
        } else {
            ListenersUtils.checkPermission(target.getLocation(), damagerPlayer, PermissionsIsland.KILL_UNKNOWN_ENTITY, event);
        }
    }
}
