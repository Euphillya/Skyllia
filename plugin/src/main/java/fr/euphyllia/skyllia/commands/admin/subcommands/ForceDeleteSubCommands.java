package fr.euphyllia.skyllia.commands.admin.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.commands.SubCommandInterface;
import fr.euphyllia.skyllia.commands.common.subcommands.DeleteSubCommand;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyllia.utils.WorldEditUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ForceDeleteSubCommands implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(ForceDeleteSubCommands.class);


    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("skyllia.admins.commands.island.delete")) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }

        if (args.length < 2) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messageADeleteCommandNotEnoughArgs);
            return true;
        }
        String playerName = args[0];
        String confirm = args[1];
        if (!confirm.equalsIgnoreCase("confirm")) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messageADeleteNotConfirmedArgs);
            return true;
        }
        try {
            UUID playerId;
            try {
                playerId = UUID.fromString(playerName);
            } catch (IllegalArgumentException ignored) {
                playerId = Bukkit.getPlayerUniqueId(playerName);
            }
            SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
            Island island = skyblockManager.getIslandByOwner(playerId).join();
            if (island == null) {
                LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerHasNotIsland);
                return true;
            }

            boolean isDisabled = island.setDisable(true);
            if (isDisabled) {
                this.updatePlayer(plugin, skyblockManager, island);

                for (WorldConfig worldConfig : ConfigToml.worldConfigs) {
                    WorldEditUtils.deleteIsland(plugin, island, Bukkit.getWorld(worldConfig.name()));
                }

                LanguageToml.sendMessage(plugin, player, LanguageToml.messageIslandDeleteSuccess);
            } else {
                LanguageToml.sendMessage(plugin, player, LanguageToml.messageError);
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            LanguageToml.sendMessage(plugin, player, LanguageToml.messageError);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }

    private void updatePlayer(Main plugin, SkyblockManager skyblockManager, Island island) {
        for (Players players : island.getMembers()) {
            players.setRoleType(RoleType.VISITOR);
            island.updateMember(players);
            DeleteSubCommand.checkClearPlayer(plugin, skyblockManager, players);
        }
    }
}
