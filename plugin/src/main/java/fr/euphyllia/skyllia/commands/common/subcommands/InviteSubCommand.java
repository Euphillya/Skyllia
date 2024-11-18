package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.PermissionManager;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyllia.cache.InviteCacheExecution;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class InviteSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(InviteSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("skyllia.island.command.invite")) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }
        if (args.length < 1) {
            LanguageToml.sendMessage(player, LanguageToml.messageInviteCommandNotEnoughArgs);
            return true;
        }
        String type = args[0];
        if (type.equalsIgnoreCase("add")) {
            if (args.length < 2) {
                LanguageToml.sendMessage(player, LanguageToml.messageInviteAddCommandNotEnoughArgs);
                return true;
            }
            String playerOrOwner = args[1];
            invitePlayer(Main.getPlugin(Main.class), player, playerOrOwner);
        } else if (type.equalsIgnoreCase("accept")) {
            if (args.length < 2) {
                LanguageToml.sendMessage(player, LanguageToml.messageInviteAcceptCommandNotEnoughArgs);
                return true;
            }
            String playerOrOwner = args[1];
            acceptPlayer(Main.getPlugin(Main.class), player, playerOrOwner);
        } else if (type.equalsIgnoreCase("decline")) {
            if (args.length < 2) {
                LanguageToml.sendMessage(player, LanguageToml.messageInviteDeclineCommandNotEnoughArgs);
                return true;
            }
            String playerOrOwner = args[1];
            declinePlayer(Main.getPlugin(Main.class), player, playerOrOwner);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> value = new ArrayList<>();
        if (args.length == 1) {
            return List.of("accept", "decline", "add");
        } else if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream().map(CommandSender::getName).collect(Collectors.toList());
        }
        return value;
    }

    private void invitePlayer(Main plugin, Player ownerIsland, String playerInvited) {
        try {
            SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
            Island island = SkylliaAPI.getCacheIslandByPlayerId(ownerIsland.getUniqueId());
            if (island == null) {
                LanguageToml.sendMessage(ownerIsland, LanguageToml.messagePlayerHasNotIsland);
                return;
            }

            Players executorPlayer = island.getMember(ownerIsland.getUniqueId());

            if (!executorPlayer.getRoleType().equals(RoleType.OWNER)) {
                PermissionRoleIsland permissionRoleIsland = skyblockManager.getPermissionIsland(island.getId(), PermissionsType.COMMANDS, executorPlayer.getRoleType()).join();

                PermissionManager permissionManager = new PermissionManager(permissionRoleIsland.permission());
                if (!permissionManager.hasPermission(PermissionsCommandIsland.INVITE)) {
                    LanguageToml.sendMessage(ownerIsland, LanguageToml.messagePlayerPermissionDenied);
                    return;
                }
            }

            UUID playerInvitedId = Bukkit.getPlayerUniqueId(playerInvited);
            if (playerInvitedId == null) {
                LanguageToml.sendMessage(ownerIsland, LanguageToml.messagePlayerNotFound);
                return;
            }

            InviteCacheExecution.addInviteCache(island.getId(), playerInvitedId);
            LanguageToml.sendMessage(ownerIsland, LanguageToml.messageInvitePlayerInvited.formatted(playerInvited));
            Player bPlayerInvited = Bukkit.getPlayer(playerInvitedId);
            if (bPlayerInvited != null && bPlayerInvited.isOnline()) {
                LanguageToml.sendMessage(bPlayerInvited, LanguageToml.messageInvitePlayerNotification.replaceAll("%player_invite%", ownerIsland.getName()));
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            LanguageToml.sendMessage(ownerIsland, LanguageToml.messageError);
        }
    }

    private void acceptPlayer(Main plugin, Player playerWantJoin, String ownerIsland) {
        try {
            Island islandPlayer = SkylliaAPI.getCacheIslandByPlayerId(playerWantJoin.getUniqueId());
            if (islandPlayer != null) {
                LanguageToml.sendMessage(playerWantJoin, LanguageToml.messageInviteAlreadyIsland);
                return;
            }
            UUID ownerIslandId = Bukkit.getPlayerUniqueId(ownerIsland);
            if (ownerIslandId == null) {
                LanguageToml.sendMessage(playerWantJoin, LanguageToml.messagePlayerNotFound);
                return;
            }
            Island islandOwner = SkylliaAPI.getCacheIslandByPlayerId(ownerIslandId);
            if (islandOwner == null) {
                LanguageToml.sendMessage(playerWantJoin, LanguageToml.messageInviteAcceptOwnerHasNotIsland);
                return;
            }
            if (!InviteCacheExecution.isInvitedCache(islandOwner.getId(), playerWantJoin.getUniqueId())) {
                LanguageToml.sendMessage(playerWantJoin, LanguageToml.messageInviteInviteNotFound);
                return;
            }
            InviteCacheExecution.removeInviteCache(islandOwner.getId(), playerWantJoin.getUniqueId());
            if (islandOwner.getMaxMembers() >= islandOwner.getMembers().size()) {
                Players newPlayers = new Players(playerWantJoin.getUniqueId(), playerWantJoin.getName(), islandOwner.getId(), RoleType.MEMBER);
                islandOwner.updateMember(newPlayers);
                LanguageToml.sendMessage(playerWantJoin, LanguageToml.messageInviteJoinIsland);

                Player ownerPlayer = Bukkit.getPlayer(ownerIslandId);
                if (ownerPlayer != null && ownerPlayer.isOnline()) {
                    LanguageToml.sendMessage(ownerPlayer, LanguageToml.messageInviteAcceptedNotification.replaceAll("%player_accept%", playerWantJoin.getName()));
                }
            } else {
                LanguageToml.sendMessage(playerWantJoin, LanguageToml.messageInviteMaxMemberExceededIsland);
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            LanguageToml.sendMessage(playerWantJoin, LanguageToml.messageError);
        }
    }

    private void declinePlayer(Main plugin, Player playerWantDecline, String ownerIsland) {
        try {
            Island island = SkylliaAPI.getCacheIslandByPlayerId(playerWantDecline.getUniqueId());

            if (island == null) {
                LanguageToml.sendMessage(playerWantDecline, LanguageToml.messagePlayerHasNotIsland);
                return;
            }
            UUID ownerIslandId = Bukkit.getPlayerUniqueId(ownerIsland);
            if (ownerIslandId == null) {
                LanguageToml.sendMessage(playerWantDecline, LanguageToml.messagePlayerNotFound);
                return;
            }
            Island islandOwner = SkylliaAPI.getCacheIslandByPlayerId(ownerIslandId);
            if (islandOwner == null) {
                LanguageToml.sendMessage(playerWantDecline, LanguageToml.messageInviteDeclineOwnerHasNotIsland);
                return;
            }
            InviteCacheExecution.removeInviteCache(islandOwner.getId(), playerWantDecline.getUniqueId());
            LanguageToml.sendMessage(playerWantDecline, LanguageToml.messageInviteDeclineDeleteInvitation.replaceAll("%player_invite%", ownerIsland));
            Player ownerPlayer = Bukkit.getPlayer(ownerIslandId);
            if (ownerPlayer != null && ownerPlayer.isOnline()) {
                LanguageToml.sendMessage(ownerPlayer, LanguageToml.messageInviteDeclinedNotification.replaceAll("%player_decline%", playerWantDecline.getName()));
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            LanguageToml.sendMessage(playerWantDecline, LanguageToml.messageError);
        }
    }
}
