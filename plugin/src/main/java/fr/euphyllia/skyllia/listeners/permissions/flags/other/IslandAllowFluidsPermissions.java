package fr.euphyllia.skyllia.listeners.permissions.flags.other;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.FlagId;
import fr.euphyllia.skyllia.api.permissions.FlagNode;
import fr.euphyllia.skyllia.api.permissions.IslandFlagRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.FlagModule;
import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.plugin.Plugin;

public class IslandAllowFluidsPermissions implements FlagModule {

    private FlagId ISLAND_ALLOW_FLUIDS;

    @EventHandler(ignoreCancelled = true)
    public void onFromTo(final BlockFromToEvent event) {
        final Location to = event.getToBlock().getLocation();
        if (!SkylliaAPI.isWorldSkyblock(to.getWorld())) return;

        final int chunkX = to.getBlockX() >> 4;
        final int chunkZ = to.getBlockZ() >> 4;
        final Island island = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
        if (island == null) return;

        if (!SkylliaAPI.getPermissionsManager().hasFlag(island, ISLAND_ALLOW_FLUIDS)) {
            event.setCancelled(true);
        }
    }

    @Override
    public void registerFlags(IslandFlagRegistry registry, Plugin owner) {
        this.ISLAND_ALLOW_FLUIDS = registry.register(new FlagNode(
                new NamespacedKey(owner, "island.allow.fluids"),
                "island.flag.allow_fluids.name",
                "island.flag.allow_fluids.description"
        ));
    }
}
