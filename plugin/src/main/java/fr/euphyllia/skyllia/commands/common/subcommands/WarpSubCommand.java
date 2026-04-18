package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.skyblock.Island;
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
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class WarpSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(WarpSubCommand.class);

    private final PermissionId ISLAND_WARP_PERMISSION;

    public WarpSubCommand() {
        this.ISLAND_WARP_PERMISSION = SkylliaAPI.getPermissionRegistry().register(new PermissionNode(
                new NamespacedKey(Skyllia.getInstance(), "command.island.warp"),
                "island.permission.command.warp.name",
                "island.permission.command.warp.description"
        ));
    }

    @Override
    public void onExecute(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return;
        }
        if (args.length < 1) {
            ConfigLoader.language.sendMessage(player, "island.warp.args-missing");
            return;
        }

        if (!PlayerUtils.hasPermission(player, "skyllia.island.command.warp")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return;
        }

        String warpName = args[0];

        try {
            Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
            if (island == null) {
                ConfigLoader.language.sendMessage(player, "island.player.no-island");
                return;
            }

            boolean allowed = SkylliaAPI.getPermissionsManager()
                    .hasPermission(player, island, ISLAND_WARP_PERMISSION, null, ConfigLoader.general.isDebugPermission());

            if (!allowed) {
                ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
                return;
            }

            WarpIsland targetWarp = island.getWarpByName(warpName);
            if (targetWarp == null) {
                ConfigLoader.language.sendMessage(player, "island.warp.warp-not-exist");
                return;
            }

            double rayon = island.getSize();
            Location center = RegionHelper.getCenterRegion(
                    Bukkit.getWorld(WorldUtils.getWorldConfigs().getFirst().getWorldName()),
                    island.getPosition().x(),
                    island.getPosition().z()
            );
            Location loc = targetWarp.location().clone();

            player.teleportAsync(loc, PlayerTeleportEvent.TeleportCause.PLUGIN).thenRun(() -> {
                player.setVelocity(new Vector(0, 0, 0));
                player.setFallDistance(0);

                ConfigLoader.language.sendMessage(player, "island.warp.teleport-success");

                if (player.hasPermission("skyllia.island.worldborder.bypass")) {
                    return;
                }

                WorldBorder border = player.getWorldBorder();
                if (border == null) {
                    border = Bukkit.createWorldBorder();
                }
                border.setCenter(center);
                border.setSize(rayon);
                player.setWorldBorder(border);
            });
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();
        if (!PlayerUtils.hasPermission(player, "skyllia.island.command.warp")) return Collections.emptyList();
        if (args.length != 1) return Collections.emptyList();

        Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
        if (island == null) return Collections.emptyList();

        List<WarpIsland> warps = island.getWarps();
        if (warps == null || warps.isEmpty()) return Collections.emptyList();

        String prefix = args[0].trim().toLowerCase(Locale.ROOT);

        return warps.stream()
                .map(WarpIsland::warpName)
                .filter(n -> n != null && n.toLowerCase(Locale.ROOT).startsWith(prefix))
                .distinct()
                .sorted()
                .limit(20)
                .toList();
    }
}