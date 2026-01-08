package fr.euphyllia.skyllia.listeners.permissions.island.flags;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.PermissionModule;
import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;

public class IslandGhastGriefFlag implements PermissionModule {

    private PermissionId ALLOW_MOB_GRIEF;
    private PermissionId ALLOW_GHAST_GRIEF;

    @Override
    public void registerPermissions(PermissionRegistry registry, Plugin owner) {
        this.ALLOW_MOB_GRIEF = registry.idOrRegister(new PermissionNode(
                new NamespacedKey(owner, "island.allow.mob-grief"),
                "Autoriser le grief des mobs (général)",
                "Placeholder"
        ));
        this.ALLOW_GHAST_GRIEF = registry.idOrRegister(new PermissionNode(
                new NamespacedKey(owner, "island.allow.ghast-grief"),
                "Autoriser les explosions de ghast",
                "Placeholder"
        ));
    }

    @EventHandler(ignoreCancelled = true)
    public void onExplode(final EntityExplodeEvent event) {
        final Entity entity = event.getEntity();
        if (!(entity instanceof Fireball fireball)) return;

        final ProjectileSource shooter = fireball.getShooter();
        if (!(shooter instanceof Ghast)) return;

        final Location location = event.getLocation();
        if (location.getWorld() == null) return;
        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;

        final Chunk chunk = location.getChunk();
        final Island island = SkylliaAPI.getIslandByChunk(chunk);
        if (island == null) return;

        final boolean allowed = SkylliaAPI.getPermissionsManager()
                .hasIslandFlag(island, ALLOW_GHAST_GRIEF, ALLOW_MOB_GRIEF);

        if (!allowed) {
            event.setCancelled(true);
        }
    }
}
