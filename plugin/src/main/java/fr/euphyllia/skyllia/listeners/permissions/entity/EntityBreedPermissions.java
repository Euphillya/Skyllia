package fr.euphyllia.skyllia.listeners.permissions.entity;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.PermissionModule;
import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.plugin.Plugin;

public class EntityBreedPermissions implements PermissionModule {

    private PermissionId ENTITY_BREED;

    @EventHandler(ignoreCancelled = true)
    public void onBreed(final EntityBreedEvent event) {
        if (!(event.getBreeder() instanceof Player player)) return;

        final Entity child = event.getEntity();
        final Location location = child.getLocation();

        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;

        final Chunk chunk = location.getChunk();
        final Island island = SkylliaAPI.getIslandByChunk(chunk);
        if (island == null) return;

        final boolean hasPermission = SkylliaAPI.getPermissionsManager().hasPermission(player, island, ENTITY_BREED);
        if (!hasPermission) {
            event.setCancelled(true);
        }
    }

    @Override
    public void registerPermissions(PermissionRegistry registry, Plugin owner) {
        this.ENTITY_BREED = registry.register(new PermissionNode(
                new NamespacedKey(owner, "entity.breed"),
                "Reproduire des animaux",
                "Placeholder"
        ));
    }
}
