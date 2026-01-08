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
import org.bukkit.entity.Enderman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.plugin.Plugin;

public class IslandAllowEndermanGriefPermissions implements PermissionModule {

    private PermissionId ISLAND_ALLOW_ENDERMAN_GRIEF;
    private PermissionId ISLAND_ALLOW_MOB_GRIEF;

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(final EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof Enderman)) return;

        final Location location = event.getBlock().getLocation();
        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;

        final Chunk chunk = location.getChunk();
        final Island island = SkylliaAPI.getIslandByChunk(chunk);
        if (island == null) return;

        final boolean allowed = SkylliaAPI.getPermissionsManager()
                .hasIslandFlag(island, ISLAND_ALLOW_ENDERMAN_GRIEF, ISLAND_ALLOW_MOB_GRIEF);

        if (!allowed) {
            event.setCancelled(true);
        }
    }

    @Override
    public void registerPermissions(PermissionRegistry registry, Plugin owner) {
        this.ISLAND_ALLOW_MOB_GRIEF = registry.register(new PermissionNode(
                new NamespacedKey(owner, "island.allow.mob-grief"),
                "Autoriser le grief des mobs (général)",
                "Placeholder"
        ));
        this.ISLAND_ALLOW_ENDERMAN_GRIEF = registry.register(new PermissionNode(
                new NamespacedKey(owner, "island.allow.enderman-grief"),
                "Autoriser le grief des endermans",
                "Placeholder"
        ));
    }
}
