package fr.euphyllia.skyllia.listeners.bukkitevents.player;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.cache.island.IslandClosedCache;
import fr.euphyllia.skyllia.cache.island.PlayersInIslandCache;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(TeleportEvent.class);

    public TeleportEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerTeleportCauseEvent(final PlayerTeleportEvent event) {
        if (event.getTo() == null) return;
        final Player player = event.getPlayer();
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL
                || event.getCause() == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT || // Peut être supprimer prochainement
                event.getCause() == PlayerTeleportEvent.TeleportCause.CONSUMABLE_EFFECT) {
            if (PermissionImp.hasPermission(player, "skyllia.player.teleport.bypass")) return;
            ListenersUtils.checkPermission(event.getTo(), event.getPlayer(), PermissionsIsland.TELEPORT, event);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerHasAccessIsland(final PlayerTeleportEvent event) {
        if (PermissionImp.hasPermission(event.getPlayer(), "skyllia.island.command.visit.bypass")) return;
        final Player player = event.getPlayer();
        Location to = event.getTo();
        World world = to.getWorld();
        if (world == null || !WorldUtils.isWorldSkyblock(world.getName())) return;

        int chunkX = to.getBlockX() >> 4;
        int chunkZ = to.getBlockZ() >> 4;

        Island island = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
        if (island == null) {
            event.setCancelled(true);
            ConfigLoader.language.sendMessage(event.getPlayer(), "island.visit.no-island-location");
            return;
        }
        if (IslandClosedCache.isIslandClosed(island.getId())) {
            var players = PlayersInIslandCache.getPlayersCached(island.getId());
            boolean isAllowed = players.stream()
                    .anyMatch(p -> p.getMojangId().equals(player.getUniqueId())
                            && p.getRoleType().getValue() >= RoleType.MEMBER.getValue());

            if (!isAllowed) {
                event.setCancelled(true);
                ConfigLoader.language.sendMessage(event.getPlayer(), "island.visit.island-closed");
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerTeleportEvent(final PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        final Location location = player.getLocation();
        final World world = location.getWorld();
        Bukkit.getAsyncScheduler().runNow(api.getPlugin(), scheduledTask -> {
            if (PermissionImp.hasPermission(player, "skyllia.island.worldborder.bypass")) return;
            if (world == null || !WorldUtils.isWorldSkyblock(world.getName())) return;
            int chunkX = location.getBlockX() >> 4;
            int chunkZ = location.getBlockZ() >> 4;

            Island island = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
            if (island == null) return;

            Location centerIsland = RegionHelper.getCenterRegion(world, island.getPosition().x(), island.getPosition().z());
            api.getPlayerNMS().setOwnWorldBorder(api.getPlugin(), player, centerIsland, island.getSize(), 0, 0);
        });
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onAddWorldBorder(final EntityAddToWorldEvent event) {
        if (event.getEntity() instanceof Player player) {
            final Location location = player.getLocation();
            final World world = location.getWorld();
            Bukkit.getAsyncScheduler().runNow(api.getPlugin(), scheduledTask -> {
                if (PermissionImp.hasPermission(player, "skyllia.island.worldborder.bypass")) return;
                if (world == null || !WorldUtils.isWorldSkyblock(world.getName())) return;
                int chunkX = location.getBlockX() >> 4;
                int chunkZ = location.getBlockZ() >> 4;

                Island island = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
                if (island == null) return;

                Location centerIsland = RegionHelper.getCenterRegion(world, island.getPosition().x(), island.getPosition().z());
                api.getPlayerNMS().setOwnWorldBorder(api.getPlugin(), player, centerIsland, island.getSize(), 0, 0);
            });
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerUsePortal(final PlayerPortalEvent event) {
        event.setCanCreatePortal(false);
        PlayerTeleportEvent.TeleportCause teleportCause = event.getCause();
        if (teleportCause == PlayerTeleportEvent.TeleportCause.END_PORTAL ||
                teleportCause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {

            Player player = event.getPlayer();
            Location location = player.getLocation();
            World world = location.getWorld();
            if (world == null) return;

            WorldConfig worldConfig = WorldUtils.getWorldConfig(world.getName());
            if (worldConfig == null) return;

            String portalRedirectWorldName = (teleportCause == PlayerTeleportEvent.TeleportCause.END_PORTAL)
                    ? worldConfig.getPortalEnd()
                    : worldConfig.getPortalNether();

            if (world.getName().equalsIgnoreCase(portalRedirectWorldName)) { // Identique world
                event.setCancelled(true);
                return;
            }

            PermissionsIsland permission = (teleportCause == PlayerTeleportEvent.TeleportCause.END_PORTAL)
                    ? PermissionsIsland.USE_END_PORTAL
                    : PermissionsIsland.USE_NETHER_PORTAL;

            ListenersUtils.checkPermission(location, player, permission, event);
        }
    }
}
