package fr.euphyllia.skyllia.listeners.permissions.flags.grief;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.FlagId;
import fr.euphyllia.skyllia.api.permissions.FlagNode;
import fr.euphyllia.skyllia.api.permissions.IslandFlagRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.FlagModule;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
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
                "island.flag.allow_mob_grief.name",
                "island.flag.allow_mob_grief.description"
        ));
        this.ALLOW_GHAST_GRIEF = registry.idOrRegister(new FlagNode(
                new NamespacedKey(owner, "island.allow.ghast-grief"),
                "island.flag.allow_ghast_grief.name",
                "island.flag.allow_ghast_grief.description"
        ));
    }

    @EventHandler(ignoreCancelled = true)
    public void onExplode(final EntityExplodeEvent event) {
        final Entity entity = event.getEntity();
        if (!(entity instanceof Fireball fireball)) return;

        final ProjectileSource shooter = fireball.getShooter();
        if (!(shooter instanceof Ghast)) return;

        final Location location = event.getLocation();
        final World world = location.getWorld();
        if (world == null) return;

        final int bx = location.getBlockX();
        final int by = location.getBlockY();
        final int bz = location.getBlockZ();

        final Island island = ListenersUtils.islandAtBlock(world, bx, bz);
        if (island == null) return;

        final String worldName = world.getName();
        if (!SkylliaAPI.getPermissionsManager().hasFlag(island, ALLOW_GHAST_GRIEF, ALLOW_MOB_GRIEF, worldName)) {
            event.setCancelled(true);
            return;
        }

        ListenersUtils.isBlockOutsideIsland(island, world, bx, by, bz, event);
    }
}
