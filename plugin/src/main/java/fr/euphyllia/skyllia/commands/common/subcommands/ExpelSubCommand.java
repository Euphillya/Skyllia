package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.PermissionManager;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ExpelSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(ExpelSubCommand.class);

    public static void expelPlayer(Main plugin, Island island, Player bPlayerToExpel, Player executor, boolean silent) {
        Location bPlayerExpelLocation = bPlayerToExpel.getLocation();
        if (Boolean.FALSE.equals(WorldUtils.isWorldSkyblock(bPlayerExpelLocation.getWorld().getName()))) {
            if (!silent) LanguageToml.sendMessage(executor, LanguageToml.messageExpelPlayerFailedNotInIsland);
            return;
        }

        org.bukkit.Bukkit.getRegionScheduler().execute(plugin, bPlayerExpelLocation, () -> {
            int chunkLocX = bPlayerExpelLocation.getChunk().getX();
            int chunkLocZ = bPlayerExpelLocation.getChunk().getZ();

            Position islandPosition = island.getPosition();
            Position playerRegionPosition = RegionHelper.getRegionInChunk(chunkLocX, chunkLocZ);

            if (islandPosition.x() != playerRegionPosition.x() || islandPosition.z() != playerRegionPosition.z()) {
                if (!silent) LanguageToml.sendMessage(executor, LanguageToml.messageExpelPlayerFailedNotInIsland);
                return;
            }

            PlayerUtils.teleportPlayerSpawn(bPlayerToExpel);
        });
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            LanguageToml.sendMessage(sender, LanguageToml.messageCommandPlayerOnly);
            return true;
        }
        if (!PermissionImp.hasPermission(sender, "skyllia.island.command.expel")) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }
        if (args.length < 1) {
            LanguageToml.sendMessage(player, LanguageToml.messageExpelCommandNotEnoughArgs);
            return true;
        }
        try {
            SkyblockManager skyblockManager = Main.getPlugin(Main.class).getInterneAPI().getSkyblockManager();
            Island island = skyblockManager.getIslandByPlayerId(player.getUniqueId()).join();

            if (island == null) {
                LanguageToml.sendMessage(player, LanguageToml.messagePlayerHasNotIsland);
                return true;
            }

            Players executorPlayer = island.getMember(player.getUniqueId());
            if (!executorPlayer.getRoleType().equals(RoleType.OWNER)) {
                PermissionRoleIsland permissionRoleIsland = skyblockManager.getPermissionIsland(island.getId(), PermissionsType.COMMANDS, executorPlayer.getRoleType()).join();

                PermissionManager permissionManager = new PermissionManager(permissionRoleIsland.permission());
                if (!permissionManager.hasPermission(PermissionsCommandIsland.EXPEL)) {
                    LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
                    return true;
                }
            }

            String playerToExpel = args[0];
            Player bPlayerToExpel = Bukkit.getPlayerExact(playerToExpel);
            if (bPlayerToExpel == null) {
                LanguageToml.sendMessage(player, LanguageToml.messagePlayerNotFound);
                return true;
            }
            if (!bPlayerToExpel.isOnline()) {
                LanguageToml.sendMessage(player, LanguageToml.messagePlayerNotConnected);
                return true;
            }
            if (PermissionImp.hasPermission(bPlayerToExpel, "skyllia.island.command.expel.bypass")) {
                LanguageToml.sendMessage(player, LanguageToml.messageExpelPlayerFailed);
                return true;
            }

            expelPlayer(Main.getPlugin(Main.class), island, bPlayerToExpel, player, false);

        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            LanguageToml.sendMessage(player, LanguageToml.messageError);
        }

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(CommandSender::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
