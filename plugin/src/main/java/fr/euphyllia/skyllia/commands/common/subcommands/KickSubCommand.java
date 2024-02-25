package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyllia.commands.SubCommandInterface;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.skyblock.PermissionManager;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KickSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(KickSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("skyllia.island.command.kick")) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }
        if (args.length < 1) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messageKickCommandNotEnoughArgs);
            return true;
        }
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
            if (!permissionManager.hasPermission(PermissionsCommandIsland.KICK)) {
                LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
                return true;
            }
        }

        String playerKick = args[0];
        Players players = island.getMember(playerKick);

        if (players == null) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerNotFound);
            return true;
        }

        if (players.getRoleType().equals(RoleType.OWNER) || executorPlayer.getRoleType().getValue() <= players.getRoleType().getValue()) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messageKickPlayerFailedHighOrEqualsStatus);
            return true;
        }

        boolean isRemoved = island.removeMember(players);
        if (isRemoved) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messageKickPlayerSuccess);
            DeleteSubCommand.checkClearPlayer(plugin, skyblockManager, players);
        } else {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messageKickPlayerFailed);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}