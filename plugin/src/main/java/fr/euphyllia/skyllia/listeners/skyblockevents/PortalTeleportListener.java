package fr.euphyllia.skyllia.listeners.skyblockevents;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.event.players.PlayerChangeWorldSkyblockEvent;
import fr.euphyllia.skyllia.api.event.players.PlayerPrepareChangeWorldSkyblockEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

public class PortalTeleportListener implements Listener {

    private final Logger logger = LogManager.getLogger(PortalTeleportListener.class);

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPrepareChangeWorldSkyblock(final PlayerPrepareChangeWorldSkyblockEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        if (event.getPortalType() == PlayerPrepareChangeWorldSkyblockEvent.PortalType.NETHER) {
            teleportOtherWorld(player, event);
        } else if (event.getPortalType() == PlayerPrepareChangeWorldSkyblockEvent.PortalType.END) {
            teleportOtherWorld(player, event);
        }
    }

    private void teleportOtherWorld(Player player, PlayerPrepareChangeWorldSkyblockEvent event) {
        Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "island.player.no-island");
            return;
        }

        WorldConfig worldConfig = event.getWorldConfig();
        String portalRedirectWorldName = switch (event.getPortalType()) {
            case NETHER -> worldConfig.getPortalNether();
            case END -> worldConfig.getPortalEnd();
            default -> null;
        };

        if (portalRedirectWorldName == null) {
            ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
            return;
        }

        if (!SkylliaAPI.isWorldSkyblock(portalRedirectWorldName)) {
            logger.error("The {} world is not a skyblock world!", portalRedirectWorldName);
            return;
        }

        World world = Bukkit.getWorld(portalRedirectWorldName);
        if (world == null) {
            logger.error("The {} world is not loaded or does not exist!", portalRedirectWorldName);
            return;
        }

        Location playerLocation = player.getLocation();

        Location center = RegionHelper.getCenterRegion(world, island.getRegionCoordinate().x(), island.getRegionCoordinate().z());
        double rayon = island.getSize();

        world.getChunkAtAsync(playerLocation.getBlockX() >> 4, playerLocation.getBlockZ() >> 4, false, false).thenAccept(ignored -> {
            Location initialLocation = findSafeLocation(world,
                    playerLocation.getBlockX(),
                    playerLocation.getBlockY(),
                    playerLocation.getBlockZ()
            );
            PlayerChangeWorldSkyblockEvent worldSkyblockEvent = new PlayerChangeWorldSkyblockEvent(
                    player, event.getPortalType(), initialLocation, true
            );
            worldSkyblockEvent.callEvent();

            Location to = worldSkyblockEvent.getTo();

            if (to == null || !WorldUtils.isSafeLocation(to)) {
                Location fallbackLocation = findSafeLocation(
                        world,
                        center.getX(),
                        playerLocation.getBlockY(),
                        center.getZ()
                );
                player.teleportAsync(fallbackLocation, PlayerTeleportEvent.TeleportCause.PLUGIN).thenAccept(success -> {
                    if (!success) return;
                    player.setVelocity(new Vector(0, 0, 0));
                    player.setFallDistance(0);
                    applyWorldBorder(player, center, rayon);
                });
            } else {
                player.teleportAsync(to, PlayerTeleportEvent.TeleportCause.PLUGIN).thenAccept(success -> {
                    if (!success) return;
                    player.setVelocity(new Vector(0, 0, 0));
                    player.setFallDistance(0);
                    applyWorldBorder(player, center, rayon);
                });
            }
        });
    }

    private void applyWorldBorder(Player player, Location center, double size) {
        if (PlayerUtils.hasPermission(player, "skyllia.island.worldborder.bypass")) return;
        WorldBorder border = player.getWorldBorder();
        if (border == null) {
            border = Bukkit.createWorldBorder();
        }
        border.setCenter(center);
        border.setSize(size);
        player.setWorldBorder(border);
    }

    private Location findSafeLocation(World world, double x, double startY, double z) {
        int maxY = world.getMaxHeight() - 2;
        int y = (int) Math.clamp(startY, world.getMinHeight(), maxY);

        Location candidate = new Location(world, x, y, z);
        while (y <= maxY) {
            candidate.setY(y);
            if (WorldUtils.isSafeLocation(candidate)) {
                return candidate;
            }
            y++;
        }

        candidate.setY(startY);
        return candidate;
    }
}