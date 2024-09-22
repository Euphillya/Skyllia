package fr.euphyllia.skyllia.commands.admin.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SetMaxMembersSubCommands implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(SetMaxMembersSubCommands.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("skyllia.admins.commands.island.setmaxmembers")) {
            LanguageToml.sendMessage(sender, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }

        if (args.length < 3) {
            LanguageToml.sendMessage(sender, LanguageToml.messageASetMaxMembersCommandNotEnoughArgs);
            return true;
        }
        String playerName = args[0];
        String changeValue = args[1];
        String confirm = args[2];
        if (!confirm.equalsIgnoreCase("confirm")) {
            LanguageToml.sendMessage(sender, LanguageToml.messageASetMaxMembersNotConfirmedArgs);
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

            int members = Integer.parseInt(changeValue);
            boolean updated = island.setMaxMembers(members);
            if (updated) {
                LanguageToml.sendMessage(sender, LanguageToml.messageASetSizeSuccess);
            } else {
                LanguageToml.sendMessage(sender, LanguageToml.messageASetSizeFailed);
            }

        } catch (Exception e) {
            if (e instanceof NumberFormatException ignored) {
                LanguageToml.sendMessage(sender, LanguageToml.messageASetMaxMembersNAN);
            } else {
                logger.log(Level.FATAL, e.getMessage(), e);
                LanguageToml.sendMessage(sender, LanguageToml.messageError);
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("skyllia.admins.commands.island.setmaxmembers")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }

        else if (args.length == 2) {
            return Arrays.asList("5", "10", "15", "20", "25");
        }

        else if (args.length == 3) {
            if ("confirm".startsWith(args[2].toLowerCase())) {
                return List.of("confirm");
            }
        }

        return new ArrayList<>();
    }
}
