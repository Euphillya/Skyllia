package fr.euphyllia.skyfolia.commands.subcommands;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.Players;
import fr.euphyllia.skyfolia.api.skyblock.model.RoleType;
import fr.euphyllia.skyfolia.commands.SubCommandInterface;
import fr.euphyllia.skyfolia.configuration.LanguageToml;
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

public class LeaveSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(LeaveSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("skyfolia.island.command.leave")) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
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
                Players players = island.getMember(player.getUniqueId());
                if (players.getRoleType().equals(RoleType.OWNER)) {
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messageLeaveFailedIsOwnerIsland);
                    return;
                }

                boolean hasLeave = island.removeMember(players);
                if (hasLeave) {
                    DeleteSubCommand.checkClearPlayer(plugin, skyblockManager, players);
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messageLeaveSuccess);
                } else {
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messageLeavePlayerFailed);
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
