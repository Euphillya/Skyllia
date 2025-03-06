package fr.euphyllia.skyllia.commands.admin.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SetSizeSubCommands implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(SetSizeSubCommands.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!PermissionImp.hasPermission(sender, "skyllia.admins.commands.island.setsize")) {
            LanguageToml.sendMessage(sender, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }

        if (args.length < 3) {
            LanguageToml.sendMessage(sender, LanguageToml.messageASetSizeCommandNotEnoughArgs);
            return true;
        }
        String playerName = args[0];
        String changeValue = args[1];
        String confirm = args[2];
        if (!confirm.equalsIgnoreCase("confirm")) {
            LanguageToml.sendMessage(sender, LanguageToml.messageASetSizeNotConfirmedArgs);
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
            Island island = skyblockManager.getIslandByPlayerId(playerId).join();
            if (island == null) {
                LanguageToml.sendMessage(sender, LanguageToml.messagePlayerHasNotIsland);
                return true;
            }

            double newSize = Double.parseDouble(changeValue);
            boolean updated = island.setSize(newSize);
            if (updated) {
                LanguageToml.sendMessage(sender, LanguageToml.messageASetSizeSuccess);
            } else {
                LanguageToml.sendMessage(sender, LanguageToml.messageASetSizeFailed);
            }

        } catch (Exception e) {
            if (e instanceof NumberFormatException ignored) {
                LanguageToml.sendMessage(sender, LanguageToml.messageASetSizeNAN);
            } else {
                logger.log(Level.FATAL, e.getMessage(), e);
                LanguageToml.sendMessage(sender, LanguageToml.messageError);
            }
        }
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!PermissionImp.hasPermission(sender, "skyllia.admins.commands.island.setsize")) {
            return Collections.emptyList();
        }

        // ---------- ARG #1 : Nom du joueur ----------
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();
            return new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .sorted()
                    .collect(Collectors.toList());
        }

        // ---------- ARG #2 : Choix de la taille ----------
        else if (args.length == 2) {
            String partial = args[1].trim().toLowerCase();
            List<String> possibleSizes = Arrays.asList("100", "200", "300", "500", "1000");

            return possibleSizes.stream()
                    .filter(size -> size.startsWith(partial))
                    .collect(Collectors.toList());
        }

        // ---------- ARG #3 : "confirm" ----------
        else if (args.length == 3) {
            String partial = args[2].trim().toLowerCase();

            return Stream.of("confirm")
                    .filter(word -> word.startsWith(partial))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
