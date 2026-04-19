package fr.euphyllia.skyllia.listeners.permissions.flags.fire;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.FlagId;
import fr.euphyllia.skyllia.api.permissions.FlagNode;
import fr.euphyllia.skyllia.api.permissions.IslandFlagRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.FlagModule;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.plugin.Plugin;

public class IslandAllowFireSpreadPermissions implements FlagModule {

    private FlagId ISLAND_ALLOW_FIRE;

    @EventHandler(ignoreCancelled = true)
    public void onSpread(final BlockSpreadEvent event) {
        if (event.getSource().getType() != Material.FIRE
                && event.getSource().getType() != Material.SOUL_FIRE) {
            return;
        }

        final Location location = event.getBlock().getLocation();
        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;

        final int chunkX = location.getBlockX() >> 4;
        final int chunkZ = location.getBlockZ() >> 4;
        final Island island = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
        if (island == null) return;

        if (!SkylliaAPI.getPermissionsManager().hasFlag(island, ISLAND_ALLOW_FIRE)) {
            event.setCancelled(true);
            return;
        }

        if (ListenersUtils.isBlockOutsideIsland(island, location, event)) {
        }
    }

    @Override
    public void registerFlags(IslandFlagRegistry registry, Plugin owner) {
        this.ISLAND_ALLOW_FIRE = registry.idOrRegister(new FlagNode(
                new NamespacedKey(owner, "island.allow.fire"),
                "island.flag.allow_fire.name",
                "island.flag.allow_fire.description"
        ));
    }
}
