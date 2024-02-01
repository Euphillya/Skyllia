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
import org.apache.logging.log4j.Level;
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

public class PromoteSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(PromoteSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("skyllia.island.command.promote")) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }
        if (args.length < 1) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messagePromoteCommandNotEnoughArgs);
            return true;
        }
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try {
            executor.execute(() -> {
                try {
                    String playerName = args[0];

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
                        if (!permissionManager.hasPermission(PermissionsCommandIsland.PROMOTE)) {
                            LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
                            return;
                        }
                    }

                    Players players = island.getMember(playerName);

                    if (players == null) {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerNotFound);
                        return;
                    }

                    if (executorPlayer.getRoleType().getValue() <= players.getRoleType().getValue()) {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messagePromotePlayerFailedLowOrEqualsStatus);
                        return;
                    }

                    RoleType promoteResult = RoleType.getRoleById(players.getRoleType().getValue() + 1);
                    if (promoteResult.getValue() == 0 || promoteResult.getValue() == RoleType.OWNER.getValue()) {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messagePromotePlayerFailed.formatted(playerName));
                        return;
                    }
                    players.setRoleType(promoteResult);
                    island.updateMember(players);
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messagePromotePlayer.formatted(playerName));
                } catch (Exception e) {
                    logger.log(Level.FATAL, e.getMessage(), e);
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messageError);
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
