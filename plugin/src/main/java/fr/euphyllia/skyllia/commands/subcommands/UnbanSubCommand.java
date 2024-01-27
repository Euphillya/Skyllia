package fr.euphyllia.skyllia.commands.subcommands;

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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class UnbanSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(UnbanSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("skyllia.island.command.unban")) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }
        if (args.length < 1) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messageUnbanCommandNotEnoughArgs);
            return true;
        }
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try {
            executor.execute(() -> {
                SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
                Island island = skyblockManager.getIslandByPlayerId(player.getUniqueId()).join();
                if (island == null) {
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerHasNotIsland);
                    return;
                }

                Players executorPlayer = island.getMember(player.getUniqueId());

                if (!executorPlayer.getRoleType().equals(RoleType.OWNER)) {
                    PermissionRoleIsland permissionRoleIsland = skyblockManager.getPermissionIsland(island.getId(), PermissionsType.COMMANDS, executorPlayer.getRoleType()).join();

                    PermissionManager permissionManager = new PermissionManager(permissionRoleIsland.permission());
                    if (!permissionManager.hasPermission(PermissionsCommandIsland.UNBAN)) {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
                        return;
                    }
                }

                String playerBan = args[0];
                Players players = island.getMember(playerBan);

                if (players == null) {
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messageUnbanPlayerNotBanned);
                    return;
                }

                boolean isRemoved = island.removeMember(players);
                if (isRemoved) {
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messageUnBanPlayerSuccess);
                } else {
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messageUnbanPlayerFailed);
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