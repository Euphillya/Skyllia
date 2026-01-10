package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.cache.commands.InviteCacheExecution;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InviteSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(InviteSubCommand.class);

    private final PermissionId ISLAND_INVITE_PERMISSION;

    public InviteSubCommand() {
        this.ISLAND_INVITE_PERMISSION = SkylliaAPI.getPermissionRegistry().register(new PermissionNode(
                new NamespacedKey(Skyllia.getInstance(), "command.island.invite"),
                "Inviter un joueur",
                "Autorise à inviter/supprimer une invitation sur l'île"
        ));
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }

        if (!player.hasPermission("skyllia.island.command.invite")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        if (args.length < 1) {
            ConfigLoader.language.sendMessage(player, "island.invite.args-missing");
            return true;
        }
        String type = args[0];

        if (type.equalsIgnoreCase("accept")) {
            if (args.length < 2) {
                ConfigLoader.language.sendMessage(player, "island.invite.accept-args-missing");
                return true;
            }
            String playerOrOwner = args[1];
            acceptPlayer(player, playerOrOwner);
        } else if (type.equalsIgnoreCase("decline")) {
            if (args.length < 2) {
                ConfigLoader.language.sendMessage(player, "island.invite.decline-args-missing");
                return true;
            }
            String playerOrOwner = args[1];
            declinePlayer(player, playerOrOwner);
        } else if (type.equalsIgnoreCase("delete")) {
            if (args.length < 2) {
                ConfigLoader.language.sendMessage(player, "island.invite.remove-args-missing");
                return true;
            }
            String playerOrOwner = args[1];
            deleteInvitePlayer(player, playerOrOwner);
        } else {
            invitePlayer(player, args[0]);
        }
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        var onlinePlayers = Bukkit.getOnlinePlayers();
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();
            return Stream.concat(
                    Stream.of("accept", "decline", "delete"),
                    onlinePlayers
                            .stream()
                            .map(Player::getName)
            ).filter(cmd -> cmd.toLowerCase().startsWith(partial)).collect(Collectors.toList());
        } else if (args.length == 2) {
            String partial = args[1].trim().toLowerCase();

            return onlinePlayers.stream()
                    .map(CommandSender::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private void deleteInvitePlayer(Player ownerIsland, String playerInvited) {
        Island island = SkylliaAPI.getIslandByPlayerId(ownerIsland.getUniqueId());

        if (island == null) {
            ConfigLoader.language.sendMessage(ownerIsland, "island.player.no-island");
            return;
        }

        boolean allowed = SkylliaAPI.getPermissionsManager().hasPermission(ownerIsland, island, ISLAND_INVITE_PERMISSION);
        if (!allowed) {
            ConfigLoader.language.sendMessage(ownerIsland, "island.player.permission-denied");
            return;
        }

        UUID playerInvitedId = Bukkit.getPlayerUniqueId(playerInvited);
        if (playerInvitedId == null) {
            ConfigLoader.language.sendMessage(ownerIsland, "island.player.not-found");
            return;
        }

        InviteCacheExecution.removeInviteCache(island.getId(), playerInvitedId);
        ConfigLoader.language.sendMessage(ownerIsland, "island.invite.invite-deleted", Map.of(
                "%s", playerInvited));
    }

    private void invitePlayer(Player ownerIsland, String playerInvited) {
        try {
            UUID playerInvitedId = Bukkit.getPlayerUniqueId(playerInvited);
            if (playerInvitedId == null) {
                ConfigLoader.language.sendMessage(ownerIsland, "island.player.not-found");
                return;
            }

            if (ownerIsland.getUniqueId().equals(playerInvitedId)) {
                ConfigLoader.language.sendMessage(ownerIsland, "island.invite.invite-yourself");
                return;
            }

            Island island = SkylliaAPI.getIslandByPlayerId(ownerIsland.getUniqueId());
            if (island == null) {
                ConfigLoader.language.sendMessage(ownerIsland, "island.player.no-island");
                return;
            }

            boolean allowed = SkylliaAPI.getPermissionsManager().hasPermission(ownerIsland, island, ISLAND_INVITE_PERMISSION);
            if (!allowed) {
                ConfigLoader.language.sendMessage(ownerIsland, "island.player.permission-denied");
                return;
            }

            InviteCacheExecution.addInviteCache(island.getId(), playerInvitedId);
            ConfigLoader.language.sendMessage(ownerIsland, "island.invite.player-invited", Map.of(
                    "%s", playerInvited));
            Player bPlayerInvited = Bukkit.getPlayer(playerInvitedId);
            if (bPlayerInvited != null && bPlayerInvited.isOnline()) {
                ConfigLoader.language.sendMessage(bPlayerInvited, "island.invite.player-notified", Map.of("%player_invite%", ownerIsland.getName()));
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            ConfigLoader.language.sendMessage(ownerIsland, "island.generic.unexpected-error");
        }
    }

    private void acceptPlayer(Player playerWantJoin, String ownerIslandName) {
        try {
            Island islandPlayer = SkylliaAPI.getIslandByPlayerId(playerWantJoin.getUniqueId());
            if (islandPlayer != null) {
                ConfigLoader.language.sendMessage(playerWantJoin, "island.invite.already-on-island");
                return;
            }

            UUID ownerId = Bukkit.getPlayerUniqueId(ownerIslandName);
            if (ownerId == null) {
                ConfigLoader.language.sendMessage(playerWantJoin, "island.player.not-found");
                return;
            }

            Island islandOwner = SkylliaAPI.getIslandByPlayerId(ownerId);
            if (islandOwner == null) {
                ConfigLoader.language.sendMessage(playerWantJoin, "island.invite.island-not-found");
                return;
            }

            if (!InviteCacheExecution.isInvitedCache(islandOwner.getId(), playerWantJoin.getUniqueId())) {
                ConfigLoader.language.sendMessage(playerWantJoin, "island.invite.invite-not-found");
                return;
            }

            InviteCacheExecution.removeInviteCache(islandOwner.getId(), playerWantJoin.getUniqueId());

            int maxMembers = islandOwner.getMaxMembers();
            int currentMembers = islandOwner.getMembers().size();

            if (currentMembers < maxMembers) {
                Players newPlayer = new Players(
                        playerWantJoin.getUniqueId(),
                        playerWantJoin.getName(),
                        islandOwner.getId(),
                        RoleType.MEMBER
                );

                boolean updated = islandOwner.updateMember(newPlayer);
                if (!updated) {
                    ConfigLoader.language.sendMessage(playerWantJoin, "island.generic.unexpected-error");
                    return;
                }

                ConfigLoader.language.sendMessage(playerWantJoin, "island.invite.join-success");

                Player ownerOnline = Bukkit.getPlayer(ownerId);
                if (ownerOnline != null && ownerOnline.isOnline()) {
                    ConfigLoader.language.sendMessage(ownerOnline, "island.invite.accept-notify-owner",
                            Map.of("%player_accept%", playerWantJoin.getName()));
                }

                if (ConfigLoader.general.isTeleportWhenAcceptingInvitation()) {
                    WarpIsland home = islandOwner.getWarpByName("home"); // cache warps TTL 5s
                    if (home == null || home.location() == null || home.location().getWorld() == null) {
                        ConfigLoader.language.sendMessage(playerWantJoin, "island.invite.home-not-found");
                        return;
                    }
                    playerWantJoin.teleportAsync(home.location(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            } else {
                ConfigLoader.language.sendMessage(playerWantJoin, "island.invite.member-limit-reached");
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            ConfigLoader.language.sendMessage(playerWantJoin, "island.generic.unexpected-error");
        }
    }

    private void declinePlayer(Player playerWantDecline, String ownerIslandName) {
        try {
            UUID ownerId = Bukkit.getPlayerUniqueId(ownerIslandName);
            if (ownerId == null) {
                ConfigLoader.language.sendMessage(playerWantDecline, "island.player.not-found");
                return;
            }

            Island islandOwner = SkylliaAPI.getIslandByPlayerId(ownerId);
            if (islandOwner == null) {
                ConfigLoader.language.sendMessage(playerWantDecline, "island.invite.island-not-found");
                return;
            }

            if (!InviteCacheExecution.isInvitedCache(islandOwner.getId(), playerWantDecline.getUniqueId())) {
                ConfigLoader.language.sendMessage(playerWantDecline, "island.invite.invite-not-found");
                return;
            }

            InviteCacheExecution.removeInviteCache(islandOwner.getId(), playerWantDecline.getUniqueId());
            ConfigLoader.language.sendMessage(playerWantDecline, "island.invite.decline-success",
                    Map.of("%player_invite%", ownerIslandName));

            Player ownerOnline = Bukkit.getPlayer(ownerId);
            if (ownerOnline != null && ownerOnline.isOnline()) {
                ConfigLoader.language.sendMessage(ownerOnline, "island.invite.decline-notify-owner",
                        Map.of("%player_decline%", playerWantDecline.getName()));
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            ConfigLoader.language.sendMessage(playerWantDecline, "island.generic.unexpected-error");
        }
    }
}
