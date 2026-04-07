package fr.euphyllia.skyllia.listeners.permissions.flags.redstone;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.FlagId;
import fr.euphyllia.skyllia.api.permissions.FlagNode;
import fr.euphyllia.skyllia.api.permissions.IslandFlagRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.FlagModule;
import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.plugin.Plugin;

public class IslandAllowPistonsExtendPermissions implements FlagModule {

    private FlagId ISLAND_ALLOW_PISTONS;

    @EventHandler(ignoreCancelled = true)
    public void onExtend(final BlockPistonExtendEvent event) {
        final Location location = event.getBlock().getLocation();
        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;

        final int chunkX = location.getBlockX() >> 4;
        final int chunkZ = location.getBlockZ() >> 4;
        final Island island = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
        if (island == null) return;

        if (!SkylliaAPI.getPermissionsManager().hasFlag(island, ISLAND_ALLOW_PISTONS)) {
            event.setCancelled(true);
        }
    }

    @Override
    public void registerFlags(IslandFlagRegistry registry, Plugin owner) {
        this.ISLAND_ALLOW_PISTONS = registry.register(new FlagNode(
                new NamespacedKey(owner, "island.allow.pistons"),
                "island.flag.allow_pistons.name",
                "island.flag.allow_pistons.description"
        ));
    }
}
