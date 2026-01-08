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
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.Plugin;

public class IslandTntGriefFlag implements PermissionModule {

    private PermissionId ALLOW_MOB_GRIEF;
    private PermissionId ALLOW_TNT_GRIEF;

    @Override
    public void registerPermissions(PermissionRegistry registry, Plugin owner) {
        this.ALLOW_MOB_GRIEF = registry.idOrRegister(new PermissionNode(
                new NamespacedKey(owner, "island.allow.mob-grief"),
                "Autoriser le grief des mobs (général)",
                "Placeholder"
        ));
        this.ALLOW_TNT_GRIEF = registry.idOrRegister(new PermissionNode(
                new NamespacedKey(owner, "island.allow.tnt-grief"),
                "Autoriser les explosions de TNT",
                "Placeholder"
        ));
    }

    @EventHandler(ignoreCancelled = true)
    public void onExplode(final EntityExplodeEvent event) {
        final Entity entity = event.getEntity();
        if (!(entity instanceof TNTPrimed) && !(entity instanceof ExplosiveMinecart)) return;

        final Location location = event.getLocation();
        if (location.getWorld() == null) return;
        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;

        final Chunk chunk = location.getChunk();
        final Island island = SkylliaAPI.getIslandByChunk(chunk);
        if (island == null) return;

        final boolean allowed = SkylliaAPI.getPermissionsManager()
                .hasIslandFlag(island, ALLOW_TNT_GRIEF, ALLOW_MOB_GRIEF);

        if (!allowed) {
            event.setCancelled(true);
        }
    }
}
