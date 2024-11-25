package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.enums.RemovalCause;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class LeaveSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(LeaveSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            LanguageToml.sendMessage(sender, LanguageToml.messageCommandPlayerOnly);
            return true;
        }
        if (!player.hasPermission("skyllia.island.command.leave")) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }

        SkyblockManager skyblockManager = Main.getPlugin(Main.class).getInterneAPI().getSkyblockManager();
        Island island = SkylliaAPI.getCacheIslandByPlayerId(player.getUniqueId());

        if (island == null) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerHasNotIsland);
            return true;
        }

        Players players = island.getMember(player.getUniqueId());

        if (players.getRoleType().equals(RoleType.OWNER)) {
            LanguageToml.sendMessage(player, LanguageToml.messageLeaveFailedIsOwnerIsland);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("confirm")) {

            boolean hasLeft = island.removeMember(players);

            if (hasLeft) {
                DeleteSubCommand.checkClearPlayer(Main.getPlugin(Main.class), skyblockManager, players, RemovalCause.LEAVE);
                LanguageToml.sendMessage(player, LanguageToml.messageLeaveSuccess);
            } else {
                LanguageToml.sendMessage(player, LanguageToml.messageLeavePlayerFailed);
            }

        } else {
            LanguageToml.sendMessage(player, LanguageToml.messageLeaveConfirmation);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            if ("confirm".startsWith(args[0].toLowerCase())) {
                return Collections.singletonList("confirm");
            }
        }
        return null;
    }
}
