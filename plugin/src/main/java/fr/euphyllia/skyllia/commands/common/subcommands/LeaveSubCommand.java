package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.commands.SubCommandInterface;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LeaveSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(LeaveSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("skyllia.island.command.leave")) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }

        SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
        Island island = skyblockManager.getIslandByPlayerId(player.getUniqueId()).join();
        if (island == null) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerHasNotIsland);
            return true;
        }
        Players players = island.getMember(player.getUniqueId());
        if (players.getRoleType().equals(RoleType.OWNER)) {
            LanguageToml.sendMessage(player, LanguageToml.messageLeaveFailedIsOwnerIsland);
            return true;
        }

        boolean hasLeave = island.removeMember(players);
        if (hasLeave) {
            DeleteSubCommand.checkClearPlayer(plugin, skyblockManager, players);
            LanguageToml.sendMessage(player, LanguageToml.messageLeaveSuccess);
        } else {
            LanguageToml.sendMessage(player, LanguageToml.messageLeavePlayerFailed);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
