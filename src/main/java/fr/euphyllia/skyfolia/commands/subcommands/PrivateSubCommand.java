package fr.euphyllia.skyfolia.commands.subcommands;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.commands.SubCommandInterface;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import fr.euphyllia.skyfolia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class PrivateSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(PrivateSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if (!(sender instanceof Player player)) {
                return true;
            }

            player.setGameMode(GameMode.SPECTATOR);
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            try {
                executor.execute(() -> {
                    SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
                    Island island = skyblockManager.getIslandByOwner(player.getUniqueId()).join();

                    if (island != null) {
                        if (island.getOwnerId().equals(player.getUniqueId())) {
                            island.setPrivateIsland(!island.isPrivateIsland());
                            player.sendMessage("Maintenant votre île est " + (island.isPrivateIsland() ? "ouverte." : "fermée."));
                        }
                    }
                });
            } finally {
                executor.shutdown();
            }
            return true;
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage());
            sender.sendMessage("Impossible de créer l'ile, vérifier les logs !");
            return false;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return ConfigToml.islandTypes.keySet().stream().toList();
        } else {
            return new ArrayList<>();
        }
    }
}
