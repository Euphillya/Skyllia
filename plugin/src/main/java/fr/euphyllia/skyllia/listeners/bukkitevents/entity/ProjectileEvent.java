package fr.euphyllia.skyllia.listeners.bukkitevents.entity;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ProjectileEvent implements Listener {

    public ProjectileEvent(InterneAPI interneAPI) {
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onUseEnderPearl(final PlayerLaunchProjectileEvent event) {
        if (event.getProjectile() instanceof EnderPearl pearl) {
            if (pearl.getShooter() instanceof Player player) {
                ListenersUtils.checkPermission(player.getLocation(), player, PermissionsIsland.USE_ENDER_PEARL, event);
            }
        }
    }
}
