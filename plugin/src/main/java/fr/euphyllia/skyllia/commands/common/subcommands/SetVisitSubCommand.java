package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.coordinate.RegionCoordinate;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SetVisitSubCommand implements SubCommandInterface {

    private static final Logger logger = LogManager.getLogger(SetVisitSubCommand.class);

    private final PermissionId ISLAND_SET_VISIT_PERMISSION;

    public SetVisitSubCommand() {
        this.ISLAND_SET_VISIT_PERMISSION = SkylliaAPI.getPermissionRegistry().register(new PermissionNode(
                new NamespacedKey(Skyllia.getInstance(), "command.island.set_visit"),
                "island.permission.command.set_visit.name",
                "island.permission.command.set_visit.description"
        ));
    }

    @Override
    public void onExecute(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return;
        }

        if (!PlayerUtils.hasPermission(player, "skyllia.island.command.setvisit")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return;
        }

        Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "island.player.no-island");
            return;
        }

        boolean allowed = SkylliaAPI.getPermissionsManager().hasPermission(player, island, ISLAND_SET_VISIT_PERMISSION, null, ConfigLoader.general.getDebugSettings().permission());
        if (!allowed) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return;
        }

        player.getScheduler().run(plugin, pScheduler -> {
            Location playerLocation = player.getLocation();
            int regionLocX = playerLocation.getChunk().getX();
            int regionLocZ = playerLocation.getChunk().getZ();

            RegionCoordinate islandPosition = island.getRegionCoordinate();
            RegionCoordinate playerRegionPosition = RegionHelper.getRegionCoordinateFromChunk(regionLocX, regionLocZ);

            if (islandPosition.x() != playerRegionPosition.x() || islandPosition.z() != playerRegionPosition.z()) {
                ConfigLoader.language.sendMessage(player, "island.player.not-on-own-island");
                return;
            }

            Bukkit.getAsyncScheduler().runNow(plugin, aScheduler -> {
                boolean success = island.setVisit(playerLocation);
                if (success) {
                    ConfigLoader.language.sendMessage(player, "island.visit.set.success");
                } else {
                    ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
                }
            });
        }, null);
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}