package fr.euphyllia.skyfolia.commands.subcommands;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.Players;
import fr.euphyllia.skyfolia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyfolia.api.skyblock.model.PermissionsIsland;
import fr.euphyllia.skyfolia.api.skyblock.model.RoleType;
import fr.euphyllia.skyfolia.commands.SubCommandInterface;
import fr.euphyllia.skyfolia.configuration.LanguageToml;
import fr.euphyllia.skyfolia.managers.skyblock.PermissionManager;
import fr.euphyllia.skyfolia.managers.skyblock.SkyblockManager;
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

public class DemoteSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(DemoteSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("skyfolia.island.command.demote")) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try {
            executor.execute(() -> {
                try {
                    String playerName = args[0];

                    SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
                    Island island = skyblockManager.getIslandByOwner(player.getUniqueId()).join();
                    if (island == null) {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerHasNotIsland);
                        return;
                    }

                    Players executorPlayer = island.getMember(player.getUniqueId());

                    PermissionRoleIsland permissionRoleIsland = skyblockManager.getPermissionIsland(island.getId(), executorPlayer.getRoleType()).join();

                    PermissionManager permissionManager = new PermissionManager(permissionRoleIsland.permission());

                    if (!permissionManager.hasPermission(PermissionsIsland.DEMOTE)) {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
                        return;
                    }

                    Players players = island.getMember(playerName);

                    if(players == null) {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerNotFound);
                        return;
                    }

                    if (players.getRoleType().equals(RoleType.OWNER) || executorPlayer.getRoleType().getValue() <= players.getRoleType().getValue()) {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messageDemotePlayerFailedHighOrEqualsStatus);
                        return;
                    }

                    RoleType demoteResult = RoleType.getRoleById(players.getRoleType().getValue() - 1);
                    if (demoteResult.getValue() == 0 || demoteResult.getValue() == -1) {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messageDemotePlayerFailed.formatted(playerName));
                        return;
                    }
                    players.setRoleType(demoteResult);
                    island.updateMember(players);
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messageDemotePlayer.formatted(playerName));
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
