package fr.euphyllia.skyllia.listeners.permissions.flags.redstone;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.FlagId;
import fr.euphyllia.skyllia.api.permissions.FlagNode;
import fr.euphyllia.skyllia.api.permissions.IslandFlagRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.FlagModule;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.plugin.Plugin;

public class IslandAllowPistonsExtendPermissions implements FlagModule {

    private FlagId ISLAND_ALLOW_PISTONS;

    @EventHandler(ignoreCancelled = true)
    public void onExtend(final BlockPistonExtendEvent event) {
        Block block = event.getBlock();
        World world = block.getWorld();
        String worldName = world.getName();
        if (!SkylliaAPI.isWorldSkyblock(worldName)) return;

        int bx = block.getX();
        int by = block.getY();
        int bz = block.getZ();

        Island island = SkylliaAPI.getIslandByChunk(bx >> 4, bz >> 4);
        if (island == null) return;

        if (!SkylliaAPI.getPermissionsManager().hasFlag(island, ISLAND_ALLOW_PISTONS, worldName)) {
            event.setCancelled(true);
            return;
        }
        ListenersUtils.isBlockOutsideIsland(island, world, bx, by, bz, event);
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
