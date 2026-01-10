package fr.euphyllia.skyllia.listeners.permissions.island.flags;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.PermissionModule;
import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Enderman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.plugin.Plugin;

public class IslandEndermanGriefFlag implements PermissionModule {

    private PermissionId ALLOW_MOB_GRIEF;
    private PermissionId ALLOW_ENDERMAN_GRIEF;

    @Override
    public void registerPermissions(PermissionRegistry registry, Plugin owner) {
        this.ALLOW_MOB_GRIEF = registry.idOrRegister(new PermissionNode(
                new NamespacedKey(owner, "island.allow.mob-grief"),
                "Autoriser le grief des mobs (général)",
                "Placeholder"
        ));
        this.ALLOW_ENDERMAN_GRIEF = registry.idOrRegister(new PermissionNode(
                new NamespacedKey(owner, "island.allow.enderman-grief"),
                "Autoriser le grief des endermans",
                "Placeholder"
        ));
    }

    @EventHandler(ignoreCancelled = true)
    public void onChangeBlock(final EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof Enderman)) return;

        final Location location = event.getBlock().getLocation();
        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;

        final Chunk chunk = location.getChunk();
        final Island island = SkylliaAPI.getIslandByChunk(chunk);
        if (island == null) return;

        final boolean allowed = SkylliaAPI.getPermissionsManager()
                .hasIslandFlag(island, ALLOW_ENDERMAN_GRIEF, ALLOW_MOB_GRIEF);

        if (!allowed) {
            event.setCancelled(true);
        }
    }
}
