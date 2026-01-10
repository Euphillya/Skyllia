package fr.euphyllia.skyllia.listeners.permissions.island;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.PermissionModule;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.plugin.Plugin;

public class IslandAllowFluidsPermissions implements PermissionModule {

    private PermissionId ISLAND_ALLOW_FLUIDS;

    @EventHandler(ignoreCancelled = true)
    public void onFromTo(final BlockFromToEvent event) {
        final Location to = event.getToBlock().getLocation();
        if (!SkylliaAPI.isWorldSkyblock(to.getWorld())) return;

        final Chunk chunk = to.getChunk();
        final Island island = SkylliaAPI.getIslandByChunk(chunk);
        if (island == null) return;

        final boolean allowed = island.getCompiledPermissions()
                .has(SkylliaAPI.getPermissionRegistry(), RoleType.ISLAND_FLAGS, ISLAND_ALLOW_FLUIDS);

        if (!allowed) {
            event.setCancelled(true);
        }
    }

    @Override
    public void registerPermissions(PermissionRegistry registry, Plugin owner) {
        this.ISLAND_ALLOW_FLUIDS = registry.register(new PermissionNode(
                new NamespacedKey(owner, "island.allow.fluids"),
                "Autoriser les fluides",
                "Placeholder"
        ));
    }
}
