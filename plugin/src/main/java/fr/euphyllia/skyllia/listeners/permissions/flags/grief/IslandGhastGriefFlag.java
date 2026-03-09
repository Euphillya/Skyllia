package fr.euphyllia.skyllia.listeners.permissions.flags.grief;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.FlagId;
import fr.euphyllia.skyllia.api.permissions.FlagNode;
import fr.euphyllia.skyllia.api.permissions.IslandFlagRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.FlagModule;
import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;

public class IslandGhastGriefFlag implements FlagModule {

    private FlagId ALLOW_MOB_GRIEF;
    private FlagId ALLOW_GHAST_GRIEF;

    @Override
    public void registerFlags(IslandFlagRegistry registry, Plugin owner) {
        this.ALLOW_MOB_GRIEF = registry.idOrRegister(new FlagNode(
                new NamespacedKey(owner, "island.allow.mob-grief"),
                "Autoriser le grief des mobs (général)", "Placeholder"
        ));
        this.ALLOW_GHAST_GRIEF = registry.idOrRegister(new FlagNode(
                new NamespacedKey(owner, "island.allow.ghast-grief"),
                "Autoriser les explosions de ghast", "Placeholder"
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

        final int chunkX = location.getBlockX() >> 4;
        final int chunkZ = location.getBlockZ() >> 4;
        final Island island = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
        if (island == null) return;

        if (!SkylliaAPI.getPermissionsManager().hasFlag(island, ALLOW_GHAST_GRIEF, ALLOW_MOB_GRIEF)) {
            event.setCancelled(true);
        }
    }
}
