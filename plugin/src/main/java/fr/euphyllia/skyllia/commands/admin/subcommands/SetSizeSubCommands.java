package fr.euphyllia.skyllia.commands.admin.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.commands.SubCommandInterface;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SetSizeSubCommands implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(SetSizeSubCommands.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("skyllia.admins.commands.island.setsize")) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }

        if (args.length < 3) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messageASetSizeCommandNotEnoughArgs);
            return true;
        }
        String playerName = args[0];
        String changeValue = args[1];
        String confirm = args[2];
        System.out.println(Arrays.toString(args));
        if (!confirm.equalsIgnoreCase("confirm")) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messageASetSizeNotConfirmedArgs);
            return true;
        }
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try {
            executor.execute(() -> {
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
                        return;
                    }

                    double newSize = Double.parseDouble(changeValue);
                    boolean updated = island.setSize(newSize);
                    if (updated) {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messageASetSizeSuccess);
                    } else {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messageASetSizeFailed);
                    }

                } catch (Exception e) {
                    if (e instanceof NumberFormatException ignored) {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messageASetSizeNAN);
                    } else {
                        logger.log(Level.FATAL, e.getMessage(), e);
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messageError);
                    }
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
