package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.enums.RemovalCause;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.PermissionsManagers;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class KickSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(KickSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            LanguageToml.sendMessage(sender, LanguageToml.messageCommandPlayerOnly);
            return true;
        }
        if (!PermissionImp.hasPermission(sender, "skyllia.island.command.kick")) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }
        if (args.length < 1) {
            LanguageToml.sendMessage(player, LanguageToml.messageKickCommandNotEnoughArgs);
            return true;
        }
        SkyblockManager skyblockManager = Main.getPlugin(Main.class).getInterneAPI().getSkyblockManager();
        Island island = skyblockManager.getIslandByPlayerId(player.getUniqueId()).join();
        if (island == null) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerHasNotIsland);
            return true;
        }

        Players executorPlayer = island.getMember(player.getUniqueId());

        if (!PermissionsManagers.testPermissions(executorPlayer, player, island, PermissionsCommandIsland.KICK, false)) {
            return true;
        }

        String playerKick = args[0];
        Players players = island.getMember(playerKick);

        if (players == null) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerNotFound);
            return true;
        }

        if (players.getRoleType().equals(RoleType.OWNER) || executorPlayer.getRoleType().getValue() <= players.getRoleType().getValue()) {
            LanguageToml.sendMessage(player, LanguageToml.messageKickPlayerFailedHighOrEqualsStatus);
            return true;
        }

        boolean isRemoved = island.removeMember(players);
        if (isRemoved) {
            LanguageToml.sendMessage(player, LanguageToml.messageKickPlayerSuccess);
            DeleteSubCommand.checkClearPlayer(Main.getPlugin(Main.class), skyblockManager, players, RemovalCause.KICKED);
        } else {
            LanguageToml.sendMessage(player, LanguageToml.messageKickPlayerFailed);
        }
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            String partial = args[0].trim().toLowerCase();
            Island island = SkylliaAPI.getCacheIslandByPlayerId(player.getUniqueId());
            if (island == null) return Collections.emptyList();
            return island.getMembersCached().stream()
                    .map(Players::getLastKnowName)
                    .filter(cmd -> cmd.toLowerCase().startsWith(partial))
                    .sorted()
                    .toList();
        }
        return Collections.emptyList();
    }
}