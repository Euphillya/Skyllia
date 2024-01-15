package fr.euphyllia.skyfolia.commands.subcommands;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.Players;
import fr.euphyllia.skyfolia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyfolia.api.skyblock.model.RoleType;
import fr.euphyllia.skyfolia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyfolia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyfolia.commands.SubCommandInterface;
import fr.euphyllia.skyfolia.configuration.LanguageToml;
import fr.euphyllia.skyfolia.managers.skyblock.PermissionManager;
import fr.euphyllia.skyfolia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class KickSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(KickSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("skyfolia.island.command.kick")) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }
        if (args.length < 1) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messageDemoteCommandNotEnoughArgs);
            return true;
        }
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try {
            executor.execute(() -> {
                SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
                Island island = skyblockManager.getIslandByOwner(player.getUniqueId()).join();
                if (island == null) {
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerHasNotIsland);
                    return;
                }

                Players executorPlayer = island.getMember(player.getUniqueId());

                PermissionRoleIsland permissionRoleIsland = skyblockManager.getPermissionIsland(island.getId(), PermissionsType.COMMANDS, executorPlayer.getRoleType()).join();

                PermissionManager permissionManager = new PermissionManager(permissionRoleIsland.permission());

                if (!permissionManager.hasPermission(PermissionsCommandIsland.KICK)) {
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
                    return;
                }
                String playerKick = args[0];
                Players players = island.getMember(playerKick);

                if (players == null) {
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerNotFound);
                    return;
                }

                if (players.getRoleType().equals(RoleType.OWNER) || executorPlayer.getRoleType().getValue() <= players.getRoleType().getValue()) {
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messageKickPlayerFailedHighOrEqualsStatus);
                    return;
                }

                boolean isRemoved = island.removeMember(players);
                if (isRemoved) {
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messageKickPlayerSuccess);
                    DeleteSubCommand.checkClearPlayer(plugin, skyblockManager, players);
                } else {
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messageKickPlayerFailed);
                }
            });
        } finally {
            executor.shutdown();
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}