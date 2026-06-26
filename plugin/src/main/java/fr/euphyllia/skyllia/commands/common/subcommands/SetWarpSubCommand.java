package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.coordinate.RegionCoordinate;
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
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class SetWarpSubCommand implements SubCommandInterface {

    private static final Logger logger = LogManager.getLogger(SetWarpSubCommand.class);

    private final PermissionId ISLAND_SET_WARP_PERMISSION;

    public SetWarpSubCommand() {
        this.ISLAND_SET_WARP_PERMISSION = SkylliaAPI.getPermissionRegistry().register(new PermissionNode(
                new NamespacedKey(Skyllia.getInstance(), "command.island.set_warp"),
                "island.permission.command.set_warp.name",
                "island.permission.command.set_warp.description"
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

        if (!PlayerUtils.hasPermission(player, "skyllia.island.command.setwarp")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return;
        }

        Location playerLocation = player.getLocation();
        if (!WorldUtils.isWorldSkyblock(playerLocation.getWorld().getName())) {
            ConfigLoader.language.sendMessage(player, "island.player.not-on-island");
            return;
        }

        Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "island.player.no-island");
            return;
        }

        String warpName = args[0];

        if (warpName.equalsIgnoreCase("spawn") || warpName.equalsIgnoreCase("home") || warpName.equalsIgnoreCase("visit")) {
            ConfigLoader.language.sendMessage(player, "island.warp.reserved-name");
            return;
        }

        boolean allowed = SkylliaAPI.getPermissionsManager().hasPermission(player, island, ISLAND_SET_WARP_PERMISSION, null, ConfigLoader.general.getDebugSettings().permission());
        if (!allowed) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return;
        }

        RegionCoordinate islandPosition = island.getRegionCoordinate();
        int chunkX = playerLocation.getBlockX() >> 4;
        int chunkZ = playerLocation.getBlockZ() >> 4;
        RegionCoordinate playerRegionPosition = RegionHelper.getRegionCoordinateFromChunk(
                chunkX,
                chunkZ
        );

        if (!islandPosition.equals(playerRegionPosition)) {
            ConfigLoader.language.sendMessage(player, "island.player.not-on-own-island");
            return;
        }

        try {
            boolean success = island.addWarps(warpName, playerLocation, false);
            if (success) {
                ConfigLoader.language.sendMessage(player, "island.warp.create-success", Map.of("%s", warpName));
            } else {
                ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return List.of();
        if (args.length != 1) return List.of();

        Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
        if (island == null) return List.of("home", "visit");

        var warps = island.getWarps();
        if (warps == null || warps.isEmpty()) return List.of("home", "visit");

        String prefix = args[0].toLowerCase();
        return warps.stream()
                .map(WarpIsland::warpName) // si record: warpName()
                .distinct()
                .filter(n -> n.toLowerCase().startsWith(prefix))
                .limit(20)
                .toList();
    }
}