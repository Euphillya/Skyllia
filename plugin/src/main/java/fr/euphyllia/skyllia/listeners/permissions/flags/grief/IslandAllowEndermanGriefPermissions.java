package fr.euphyllia.skyllia.listeners.permissions.flags.grief;

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
import org.bukkit.entity.Enderman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.plugin.Plugin;

public class IslandAllowEndermanGriefPermissions implements FlagModule {

    private FlagId ISLAND_ALLOW_ENDERMAN_GRIEF;
    private FlagId ISLAND_ALLOW_MOB_GRIEF;

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(final EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof Enderman)) return;

        final Block block = event.getBlock();
        final World world = block.getWorld();

        final int bx = block.getX();
        final int by = block.getY();
        final int bz = block.getZ();

        final Island island = ListenersUtils.islandAtBlock(world, bx, bz);
        if (island == null) return;

        final String worldName = world.getName();
        if (!SkylliaAPI.getPermissionsManager().hasFlag(island, ISLAND_ALLOW_ENDERMAN_GRIEF, ISLAND_ALLOW_MOB_GRIEF, worldName)) {
            event.setCancelled(true);
            return;
        }

        ListenersUtils.isBlockOutsideIsland(island, world, bx, by, bz, event);
    }

    @Override
    public void registerFlags(IslandFlagRegistry registry, Plugin owner) {
        this.ISLAND_ALLOW_MOB_GRIEF = registry.idOrRegister(new FlagNode(
                new NamespacedKey(owner, "island.allow.mob-grief"),
                "island.flag.allow_mob_grief.name",
                "island.flag.allow_mob_grief.description"
        ));
        this.ISLAND_ALLOW_ENDERMAN_GRIEF = registry.register(new FlagNode(
                new NamespacedKey(owner, "island.allow.enderman-grief"),
                "island.flag.allow_enderman_grief.name",
                "island.flag.allow_enderman_grief.description"
        ));
    }
}
