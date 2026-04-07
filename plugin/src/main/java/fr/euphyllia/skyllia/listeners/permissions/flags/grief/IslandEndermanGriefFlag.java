package fr.euphyllia.skyllia.listeners.permissions.flags.grief;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.FlagId;
import fr.euphyllia.skyllia.api.permissions.FlagNode;
import fr.euphyllia.skyllia.api.permissions.IslandFlagRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.FlagModule;
import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Enderman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.plugin.Plugin;

public class IslandEndermanGriefFlag implements FlagModule {

    private FlagId ALLOW_MOB_GRIEF;
    private FlagId ALLOW_ENDERMAN_GRIEF;

    @Override
    public void registerFlags(IslandFlagRegistry registry, Plugin owner) {
        this.ALLOW_MOB_GRIEF = registry.idOrRegister(new FlagNode(
                new NamespacedKey(owner, "island.allow.mob-grief"),
                "island.flag.allow_mob_grief.name",
                "island.flag.allow_mob_grief.description"
        ));
        this.ALLOW_ENDERMAN_GRIEF = registry.idOrRegister(new FlagNode(
                new NamespacedKey(owner, "island.allow.enderman-grief"),
                "island.flag.allow_enderman_grief.name",
                "island.flag.allow_enderman_grief.description"
        ));
    }

    @EventHandler(ignoreCancelled = true)
    public void onChangeBlock(final EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof Enderman)) return;

        final Location location = event.getBlock().getLocation();
        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;

        final int chunkX = location.getBlockX() >> 4;
        final int chunkZ = location.getBlockZ() >> 4;
        final Island island = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
        if (island == null) return;

        if (!SkylliaAPI.getPermissionsManager().hasFlag(island, ALLOW_ENDERMAN_GRIEF, ALLOW_MOB_GRIEF)) {
            event.setCancelled(true);
        }
    }
}
