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
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;


public class SetSpawnSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(SetSpawnSubCommand.class);

    private final PermissionId ISLAND_SET_SPAWN_PERMISSION;

    public SetSpawnSubCommand() {
        this.ISLAND_SET_SPAWN_PERMISSION = SkylliaAPI.getPermissionRegistry().register(new PermissionNode(
                new NamespacedKey(Skyllia.getInstance(), "command.island.set_spawn"),
                "island.permission.command.set_spawn.name",
                "island.permission.command.set_spawn.description"
        ));
    }

    @Override
    public void onExecute(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return;
        }

        if (!PlayerUtils.hasPermission(player, "skyllia.island.command.setspawn")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return;
        }

        Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "island.player.no-island");
            return;
        }

        RegionCoordinate islandPosition = island.getRegionCoordinate();

        boolean allowed = SkylliaAPI.getPermissionsManager().hasPermission(
                player, island, ISLAND_SET_SPAWN_PERMISSION, null,
                ConfigLoader.general.getDebugSettings().permission());
        if (!allowed) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return;
        }

        Location playerLocation = player.getLocation();
        int chunkX = playerLocation.getBlockX() >> 4;
        int chunkZ = playerLocation.getBlockZ() >> 4;

        RegionCoordinate playerRegionPosition = RegionHelper.getRegionCoordinateFromChunk(chunkX, chunkZ);
        if (islandPosition.x() != playerRegionPosition.x() || islandPosition.z() != playerRegionPosition.z()) {
            ConfigLoader.language.sendMessage(player, "island.player.not-on-own-island");
            return;
        }

        boolean success = island.setSpawnLocation(playerLocation);
        if (success) {
            ConfigLoader.language.sendMessage(player, "island.spawn.set.success");
        } else {
            ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}