package fr.euphyllia.skyllia.commands.admin.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
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
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ForceDeleteSubCommands implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(ForceDeleteSubCommands.class);


    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("skyllia.admins.commands.island.delete")) {
            LanguageToml.sendMessage(sender, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }

        if (args.length < 2) {
            LanguageToml.sendMessage(sender, LanguageToml.messageADeleteCommandNotEnoughArgs);
            return true;
        }
        String playerName = args[0];
        String confirm = args[1];
        if (!confirm.equalsIgnoreCase("confirm")) {
            LanguageToml.sendMessage(sender, LanguageToml.messageADeleteNotConfirmedArgs);
            return true;
        }
        try {
            UUID playerId;
            try {
                playerId = UUID.fromString(playerName);
            } catch (IllegalArgumentException ignored) {
                playerId = Bukkit.getPlayerUniqueId(playerName);
            }
            SkyblockManager skyblockManager = Main.getPlugin(Main.class).getInterneAPI().getSkyblockManager();
            Island island = skyblockManager.getIslandByOwner(playerId).join();
            if (island == null) {
                LanguageToml.sendMessage(sender, LanguageToml.messagePlayerHasNotIsland);
                return true;
            }

            boolean isDisabled = island.setDisable(true);
            if (isDisabled) {
                this.updatePlayer(Main.getPlugin(Main.class), skyblockManager, island);

                for (WorldConfig worldConfig : ConfigToml.worldConfigs) {
                    WorldEditUtils.deleteIsland(Main.getPlugin(Main.class), island, Bukkit.getWorld(worldConfig.name()));
                }

                LanguageToml.sendMessage(sender, LanguageToml.messageIslandDeleteSuccess);
            } else {
                LanguageToml.sendMessage(sender, LanguageToml.messageError);
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            LanguageToml.sendMessage(sender, LanguageToml.messageError);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("skyllia.admins.commands.island.delete")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }

        else if (args.length == 2) {
            return List.of("confirm");
        }

        return new ArrayList<>();
    }

    private void updatePlayer(Main plugin, SkyblockManager skyblockManager, Island island) {
        for (Players players : island.getMembers()) {
            players.setRoleType(RoleType.VISITOR);
            island.updateMember(players);
            DeleteSubCommand.checkClearPlayer(plugin, skyblockManager, players);
        }
    }
}
