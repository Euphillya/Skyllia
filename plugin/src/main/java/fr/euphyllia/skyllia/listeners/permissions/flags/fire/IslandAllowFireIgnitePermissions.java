package fr.euphyllia.skyllia.listeners.permissions.flags.fire;

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
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.plugin.Plugin;

public class IslandAllowFireIgnitePermissions implements FlagModule {

    private FlagId ISLAND_ALLOW_FIRE;

    @EventHandler(ignoreCancelled = true)
    public void onIgnite(final BlockIgniteEvent event) {
        final Block block = event.getBlock();
        final World world = block.getWorld();

        final int bx = block.getX();
        final int by = block.getY();
        final int bz = block.getZ();

        final Island island = ListenersUtils.islandAtBlock(world, bx, bz);
        if (island == null) return;

        final String worldName = world.getName();
        if (!SkylliaAPI.getPermissionsManager().hasFlag(island, ISLAND_ALLOW_FIRE, worldName)) {
            event.setCancelled(true);
            return;
        }

        ListenersUtils.isBlockOutsideIsland(island, world, bx, by, bz, event);
    }

    @Override
    public void registerFlags(IslandFlagRegistry registry, Plugin owner) {
        this.ISLAND_ALLOW_FIRE = registry.register(new FlagNode(
                new NamespacedKey(owner, "island.allow.fire"),
                "island.flag.allow_fire.name",
                "island.flag.allow_fire.description"
        ));
    }
}
