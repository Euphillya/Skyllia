package fr.euphyllia.skyllia.listeners.permissions.player;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.event.teleport.PlayerTeleportIslandEvent;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.PermissionModule;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.WorldBorder;
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
                "island.permission.player_teleport.name",
                "island.permission.player_teleport.description"
        ));
    }

    @EventHandler
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        Location to = event.getTo();
        if (!SkylliaAPI.isWorldSkyblock(to.getWorld())) return;

        final int chunkX = to.getBlockX() >> 4;
        final int chunkZ = to.getBlockZ() >> 4;
        final Island island = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
        if (island == null) return;

        PlayerTeleportIslandEvent playerTeleportIslandEvent = new PlayerTeleportIslandEvent(
                player,
                event.getFrom(),
                to,
                island,
                event.getCause(),
                event.isCancelled(),
                false
        );
        playerTeleportIslandEvent.callEvent();
        if (playerTeleportIslandEvent.isCancelled()) {
            event.setCancelled(true);
        }
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
            final boolean hasPermission = SkylliaAPI.getPermissionsManager().hasPermission(player, island, TELEPORT, "skyllia.player.teleport.bypass", ConfigLoader.general.isDebugPermission());
            if (!hasPermission) {
                ConfigLoader.language.sendMessage(event.getPlayer(), "island.visit.island-closed");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onAddWorldBorder(final PlayerTeleportIslandEvent event) {
        final Player player = event.getPlayer();

        if (player.hasPermission("skyllia.island.worldborder.bypass")) {
            return;
        }

        final Location to = event.getTo();
        final Island island = event.getIsland();

        if (island == null) return;

        Location centerIsland = RegionHelper.getCenterRegion(to.getWorld(), island.getPosition().x(), island.getPosition().z());

        WorldBorder border = player.getWorldBorder();
        if (border == null) {
            border = Bukkit.createWorldBorder();
        }
        border.setCenter(centerIsland);
        border.setSize(island.getSize());
        player.setWorldBorder(border);
    }
}