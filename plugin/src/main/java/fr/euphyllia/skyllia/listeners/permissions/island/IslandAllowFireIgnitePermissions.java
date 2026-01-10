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
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.plugin.Plugin;

public class IslandAllowFireIgnitePermissions implements PermissionModule {

    private PermissionId ISLAND_ALLOW_FIRE;

    @EventHandler(ignoreCancelled = true)
    public void onIgnite(final BlockIgniteEvent event) {
        final Location location = event.getBlock().getLocation();
        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;

        final Chunk chunk = location.getChunk();
        final Island island = SkylliaAPI.getIslandByChunk(chunk);
        if (island == null) return;

        final boolean allowed = island.getCompiledPermissions()
                .has(SkylliaAPI.getPermissionRegistry(), RoleType.VISITOR, ISLAND_ALLOW_FIRE);

        if (!allowed) {
            event.setCancelled(true);
        }
    }

    @Override
    public void registerPermissions(PermissionRegistry registry, Plugin owner) {
        this.ISLAND_ALLOW_FIRE = registry.register(new PermissionNode(
                new NamespacedKey(owner, "island.allow.fire"),
                "Autoriser le feu",
                "Placeholder"
        ));
    }
}
