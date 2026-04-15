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
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.Plugin;

public class IslandWitherGriefFlag implements FlagModule {

    private FlagId ALLOW_MOB_GRIEF;
    private FlagId ALLOW_WITHER_GRIEF;

    @Override
    public void registerFlags(IslandFlagRegistry registry, Plugin owner) {
        this.ALLOW_MOB_GRIEF = registry.idOrRegister(new FlagNode(
                new NamespacedKey(owner, "island.allow.mob-grief"),
                "island.flag.allow_mob_grief.name",
                "island.flag.allow_mob_grief.description"
        ));
        this.ALLOW_WITHER_GRIEF = registry.idOrRegister(new FlagNode(
                new NamespacedKey(owner, "island.allow.wither-grief"),
                "island.flag.allow_wither_grief.name",
                "island.flag.allow_wither_grief.description"
        ));
    }

    @EventHandler(ignoreCancelled = true)
    public void onExplode(final EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof Wither)) return;

        final Location location = event.getLocation();
        if (location.getWorld() == null) return;
        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;

        final int chunkX = location.getBlockX() >> 4;
        final int chunkZ = location.getBlockZ() >> 4;
        final Island island = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
        if (island == null) return;

        if (!SkylliaAPI.getPermissionsManager().hasFlag(island, ALLOW_WITHER_GRIEF, ALLOW_MOB_GRIEF)) {
            event.setCancelled(true);
            return;
        }

        if (ListenersUtils.isBlockOutsideIsland(island, location, event)) {
            return;
        }
    }
}
