package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.commands.SubCommandInterface;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.skyblock.PermissionManager;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ExpelSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(ExpelSubCommand.class);

    public static void expelPlayer(Main plugin, Island island, Player bPlayerToExpel, Player executor, boolean silent) {
        Location bPlayerExpelLocation = bPlayerToExpel.getLocation();
        if (Boolean.FALSE.equals(WorldUtils.isWorldSkyblock(bPlayerExpelLocation.getWorld().getName()))) {
            if (!silent) LanguageToml.sendMessage(plugin, executor, LanguageToml.messageExpelPlayerFailedNotInIsland);
            return;
        }

        int chunkLocX = bPlayerExpelLocation.getChunk().getX();
        int chunkLocZ = bPlayerExpelLocation.getChunk().getZ();

        Position islandPosition = island.getPosition();
        Position playerRegionPosition = RegionHelper.getRegionInChunk(chunkLocX, chunkLocZ);

        if (islandPosition.x() != playerRegionPosition.x() || islandPosition.z() != playerRegionPosition.z()) {
            if (!silent) LanguageToml.sendMessage(plugin, executor, LanguageToml.messageExpelPlayerFailedNotInIsland);
            return;
        }

        PlayerUtils.teleportPlayerSpawn(plugin, bPlayerToExpel);
    }

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("skyllia.island.command.expel")) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }
        if (args.length < 1) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messageExpelCommandNotEnoughArgs);
            return true;
        }
        try {
            SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
            Island island = skyblockManager.getIslandByPlayerId(player.getUniqueId()).join();

            if (island == null) {
                LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerHasNotIsland);
                return true;
            }

            Players executorPlayer = island.getMember(player.getUniqueId());
            if (!executorPlayer.getRoleType().equals(RoleType.OWNER)) {
                PermissionRoleIsland permissionRoleIsland = skyblockManager.getPermissionIsland(island.getId(), PermissionsType.COMMANDS, executorPlayer.getRoleType()).join();

                PermissionManager permissionManager = new PermissionManager(permissionRoleIsland.permission());
                if (!permissionManager.hasPermission(PermissionsCommandIsland.EXPEL)) {
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
                    return true;
                }
            }

            String playerToExpel = args[0];
            Player bPlayerToExpel = Bukkit.getPlayerExact(playerToExpel);
            if (bPlayerToExpel == null) {
                LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerNotFound);
                return true;
            }
            if (!bPlayerToExpel.isOnline()) {
                LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerNotConnected);
                return true;
            }
            if (bPlayerToExpel.hasPermission("skyllia.island.command.expel.bypass")) {
                LanguageToml.sendMessage(plugin, player, LanguageToml.messageExpelPlayerFailed);
                return true;
            }

            expelPlayer(plugin, island, bPlayerToExpel, player, false);

        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            LanguageToml.sendMessage(plugin, player, LanguageToml.messageError);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
