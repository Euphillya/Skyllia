package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyllia.cache.InviteCacheExecution;
import fr.euphyllia.skyllia.commands.SubCommandInterface;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.api.skyblock.PermissionManager;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class InviteSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(InviteSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("skyllia.island.command.invite")) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }
        if (args.length < 1) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messageInviteCommandNotEnoughArgs);
            return true;
        }
        String type = args[0];
        if (type.equalsIgnoreCase("add")) {
            if (args.length < 2) {
                LanguageToml.sendMessage(plugin, player, LanguageToml.messageInviteAddCommandNotEnoughArgs);
                return true;
            }
            String playerOrOwner = args[1];
            invitePlayer(plugin, player, playerOrOwner);
        } else if (type.equalsIgnoreCase("accept")) {
            if (args.length < 2) {
                LanguageToml.sendMessage(plugin, player, LanguageToml.messageInviteAcceptCommandNotEnoughArgs);
                return true;
            }
            String playerOrOwner = args[1];
            acceptPlayer(plugin, player, playerOrOwner);
        } else if (type.equalsIgnoreCase("decline")) {
            if (args.length < 2) {
                LanguageToml.sendMessage(plugin, player, LanguageToml.messageInviteDeclineCommandNotEnoughArgs);
                return true;
            }
            String playerOrOwner = args[1];
            declinePlayer(plugin, player, playerOrOwner);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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
            Island island = skyblockManager.getIslandByPlayerId(ownerIsland.getUniqueId()).join();
            if (island == null) {
                LanguageToml.sendMessage(plugin, ownerIsland, LanguageToml.messagePlayerHasNotIsland);
                return;
            }

            Players executorPlayer = island.getMember(ownerIsland.getUniqueId());

            if (!executorPlayer.getRoleType().equals(RoleType.OWNER)) {
                PermissionRoleIsland permissionRoleIsland = skyblockManager.getPermissionIsland(island.getId(), PermissionsType.COMMANDS, executorPlayer.getRoleType()).join();

                PermissionManager permissionManager = new PermissionManager(permissionRoleIsland.permission());
                if (!permissionManager.hasPermission(PermissionsCommandIsland.INVITE)) {
                    LanguageToml.sendMessage(plugin, ownerIsland, LanguageToml.messagePlayerPermissionDenied);
                    return;
                }
            }

            UUID playerInvitedId = Bukkit.getPlayerUniqueId(playerInvited);
            if (playerInvitedId == null) {
                LanguageToml.sendMessage(plugin, ownerIsland, LanguageToml.messagePlayerNotFound);
                return;
            }

            InviteCacheExecution.addInviteCache(island.getId(), playerInvitedId);
            LanguageToml.sendMessage(plugin, ownerIsland, LanguageToml.messageInvitePlayerInvited.formatted(playerInvited));
            Player bPlayerInvited = Bukkit.getPlayer(playerInvitedId);
            if (bPlayerInvited != null && bPlayerInvited.isOnline()) {
                LanguageToml.sendMessage(plugin, bPlayerInvited, LanguageToml.messageInvitePlayerNotification.replaceAll("%player_invite%", ownerIsland.getName()));
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            LanguageToml.sendMessage(plugin, ownerIsland, LanguageToml.messageError);
        }
    }

    private void acceptPlayer(Main plugin, Player playerWantJoin, String ownerIsland) {
        try {
            SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
            Island islandPlayer = skyblockManager.getIslandByPlayerId(playerWantJoin.getUniqueId()).join();
            if (islandPlayer != null) {
                LanguageToml.sendMessage(plugin, playerWantJoin, LanguageToml.messageInviteAlreadyIsland);
                return;
            }
            UUID ownerIslandId = Bukkit.getPlayerUniqueId(ownerIsland);
            if (ownerIslandId == null) {
                LanguageToml.sendMessage(plugin, playerWantJoin, LanguageToml.messagePlayerNotFound);
                return;
            }
            Island islandOwner = skyblockManager.getIslandByOwner(ownerIslandId).join();
            if (islandOwner == null) {
                LanguageToml.sendMessage(plugin, playerWantJoin, LanguageToml.messageInviteAcceptOwnerHasNotIsland);
                return;
            }
            InviteCacheExecution.removeInviteCache(islandOwner.getId(), playerWantJoin.getUniqueId());
            if (islandOwner.getMaxMembers() >= islandOwner.getMembers().size()) {
                Players newPlayers = new Players(playerWantJoin.getUniqueId(), playerWantJoin.getName(), islandOwner.getId(), RoleType.MEMBER);
                islandOwner.updateMember(newPlayers);
                LanguageToml.sendMessage(plugin, playerWantJoin, LanguageToml.messageInviteJoinIsland);
            } else {
                LanguageToml.sendMessage(plugin, playerWantJoin, LanguageToml.messageInviteMaxMemberExceededIsland);
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            LanguageToml.sendMessage(plugin, playerWantJoin, LanguageToml.messageError);
        }
    }

    private void declinePlayer(Main plugin, Player playerWantDecline, String ownerIsland) {
        try {
            SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
            Island island = skyblockManager.getIslandByPlayerId(playerWantDecline.getUniqueId()).join();
            if (island == null) {
                LanguageToml.sendMessage(plugin, playerWantDecline, LanguageToml.messagePlayerHasNotIsland);
                return;
            }
            UUID ownerIslandId = Bukkit.getPlayerUniqueId(ownerIsland);
            if (ownerIslandId == null) {
                LanguageToml.sendMessage(plugin, playerWantDecline, LanguageToml.messagePlayerNotFound);
                return;
            }
            Island islandOwner = skyblockManager.getIslandByOwner(ownerIslandId).join();
            if (islandOwner == null) {
                LanguageToml.sendMessage(plugin, playerWantDecline, LanguageToml.messageInviteDeclineOwnerHasNotIsland);
                return;
            }
            InviteCacheExecution.removeInviteCache(islandOwner.getId(), playerWantDecline.getUniqueId());
            LanguageToml.sendMessage(plugin, playerWantDecline, LanguageToml.messageInviteDeclineDeleteInvitation.replaceAll("%player_invite%", ownerIsland));
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            LanguageToml.sendMessage(plugin, playerWantDecline, LanguageToml.messageError);
        }
    }
}
