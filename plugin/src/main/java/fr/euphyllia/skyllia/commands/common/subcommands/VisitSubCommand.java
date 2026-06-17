package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.WorldBorder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class VisitSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(VisitSubCommand.class);

    private final PermissionId ISLAND_VISIT_BYPASS_PERMISSION;

    public VisitSubCommand() {
        this.ISLAND_VISIT_BYPASS_PERMISSION = SkylliaAPI.getPermissionRegistry().register(new PermissionNode(
                new NamespacedKey(Skyllia.getInstance(), "command.island.visit.bypass"),
                "island.permission.command.visit_bypass.name",
                "island.permission.command.visit_bypass.description"
        ));
    }

    @Override
    public void onExecute(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return;
        }

        if (!PlayerUtils.hasPermission(player, "skyllia.island.command.visit")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return;
        }

        if (args.length < 1) {
            ConfigLoader.language.sendMessage(player, "island.visit.args-missing");
            return;
        }

        try {
            String visitPlayer = args[0];
            UUID visitPlayerId;
            try {
                visitPlayerId = UUID.fromString(visitPlayer);
            } catch (IllegalArgumentException ignored) {
                visitPlayerId = Bukkit.getPlayerUniqueId(visitPlayer);
            }
            if (visitPlayerId == null) {
                ConfigLoader.language.sendMessage(player, "island.player.not-found");
                return;
            }

            Island island = SkylliaAPI.getIslandByPlayerId(visitPlayerId);
            if (island == null) {
                ConfigLoader.language.sendMessage(player, "island.visit.no-island");
                return;
            }

            boolean bypass = PlayerUtils.hasPermission(player, "skyllia.island.command.visit.bypass")
                    || SkylliaAPI.getPermissionsManager().hasPermission(player, island, ISLAND_VISIT_BYPASS_PERMISSION, null, ConfigLoader.general.getDebugSettings().permission());

            if (!bypass) {
                if (island.isPrivateIsland()) {
                    ConfigLoader.language.sendMessage(player, "island.visit.island-private");
                    return;
                }
                Players memberIsland = island.getMember(player.getUniqueId());
                if (memberIsland != null && memberIsland.getRoleType().equals(RoleType.BAN)) {
                    ConfigLoader.language.sendMessage(player, "island.visit.banned");
                    return;
                }
            }

            WarpIsland warpIsland = island.getVisit();

            player.getScheduler().execute(plugin, () -> {
                Location loc;
                if (warpIsland == null) {
                    loc = RegionHelper.getCenterRegion(Bukkit.getWorld(WorldUtils.getWorldConfigs().getFirst().getWorldName()), island.getRegionCoordinate().x(), island.getRegionCoordinate().z());
                } else {
                    loc = warpIsland.location();
                }
                loc.setY(loc.getY() + 0.5);
                player.teleportAsync(loc, PlayerTeleportEvent.TeleportCause.PLUGIN).thenRun(() -> {
                    if (PlayerUtils.hasPermission(player, "skyllia.island.worldborder.bypass")) {
                        return;
                    }

                    ConfigLoader.language.sendMessage(player, "island.visit.success", Map.of(
                            "%player%", visitPlayer));

                    Location center = RegionHelper.getCenterRegion(loc.getWorld(), island.getRegionCoordinate().x(), island.getRegionCoordinate().z());

                    WorldBorder border = player.getWorldBorder();
                    if (border == null) {
                        border = Bukkit.createWorldBorder();
                    }

                    border.setCenter(center);
                    border.setSize(island.getSize());
                    player.setWorldBorder(border);
                });
            }, null, 1L);
        } catch (Exception exception) {
            logger.log(Level.FATAL, exception.getMessage(), exception);
            ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();
            var onlinePlayers = Bukkit.getOnlinePlayers();
            List<String> list = new ArrayList<>();
            for (Player onlinePlayer : onlinePlayers) {
                String name = onlinePlayer.getName();
                if (name.toLowerCase().startsWith(partial)) {
                    list.add(name);
                }
            }
            return list;
        }
        return Collections.emptyList();
    }
}