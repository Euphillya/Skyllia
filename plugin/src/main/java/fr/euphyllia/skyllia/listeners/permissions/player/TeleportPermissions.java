package fr.euphyllia.skyllia.listeners.permissions.player;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.event.teleport.PlayerTeleportIslandEvent;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.PermissionModule;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

public class TeleportPermissions implements PermissionModule {

    private PermissionId TELEPORT;

    @Override
    public void registerPermissions(PermissionRegistry registry, Plugin owner) {
        this.TELEPORT = registry.register(new PermissionNode(
                new NamespacedKey(owner, "player.teleport"),
                "Téléporter vers une île",
                "Placeholder"
        ));
    }

    @EventHandler
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        // Implementation basic
        final Player player = event.getPlayer();
        Location to = event.getTo();
        if (!SkylliaAPI.isWorldSkyblock(to.getWorld())) return;

        Island island = SkylliaAPI.getIslandByChunk(to.getChunk());
        if (island == null) return;

        new PlayerTeleportIslandEvent(
                player,
                event.getFrom(),
                to,
                island,
                event.getCause(),
                event.isCancelled(),
                false
        ).callEvent();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerHasAccessIsland(final PlayerTeleportIslandEvent event) {
        final Player player = event.getPlayer();
        final Island island = event.getIsland();

        if (island == null) {
            ConfigLoader.language.sendMessage(event.getPlayer(), "island.visit.no-island-location");
            event.setCancelled(true);
            return;
        }

        if (island.isPrivateIsland()) {
            final boolean hasPermission = SkylliaAPI.getPermissionsManager().hasPermission(player, island, TELEPORT);
            if (!hasPermission) {
                ConfigLoader.language.sendMessage(event.getPlayer(), "island.visit.island-closed");
                event.setCancelled(true);
            }
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onAddWorldBorder(final PlayerTeleportIslandEvent event) {
        final Player player = event.getPlayer();
        final Location to = event.getTo();
        final Island island = event.getIsland();

        if (island == null) return;

        Location centerIsland = RegionHelper.getCenterRegion(to.getWorld(), island.getPosition().x(), island.getPosition().z());
        Skyllia.getInstance().getInterneAPI().getPlayerNMS().setOwnWorldBorder(Skyllia.getInstance(), player, centerIsland, island.getSize(), 0, 0);

    }
}
