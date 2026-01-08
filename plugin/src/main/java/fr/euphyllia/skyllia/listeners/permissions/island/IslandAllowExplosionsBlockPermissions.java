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
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.plugin.Plugin;

public class IslandAllowExplosionsBlockPermissions implements PermissionModule {

    private PermissionId ISLAND_ALLOW_EXPLOSIONS;

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(final BlockExplodeEvent event) {
        final Location location = event.getBlock().getLocation();
        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;

        final Chunk chunk = location.getChunk();
        final Island island = SkylliaAPI.getIslandByChunk(chunk);
        if (island == null) return;

        final boolean allowed = island.getCompiledPermissions()
                .has(SkylliaAPI.getPermissionRegistry(), RoleType.ISLAND_FLAGS, ISLAND_ALLOW_EXPLOSIONS);

        if (!allowed) {
            event.setCancelled(true);
        }
    }

    @Override
    public void registerPermissions(PermissionRegistry registry, Plugin owner) {
        this.ISLAND_ALLOW_EXPLOSIONS = registry.register(new PermissionNode(
                new NamespacedKey(owner, "island.allow.explosions"),
                "Autoriser les explosions",
                "Placeholder"
        ));
    }
}
