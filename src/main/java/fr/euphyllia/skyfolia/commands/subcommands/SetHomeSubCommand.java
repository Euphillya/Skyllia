package fr.euphyllia.skyfolia.commands.subcommands;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.commands.SubCommandInterface;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SetHomeSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(SetHomeSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        Location playerLocation = player.getLocation();
        if (!isWorldIsland(playerLocation.getWorld().getName())) {
            sender.sendMessage("Vous n'êtes pas sur votre ile");
            return true;
        }

        int regionLocX = playerLocation.getChunk().getX();
        int regionLocZ = playerLocation.getChunk().getZ();

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try {
            executor.execute(() -> SetWarpSubCommand.setWarpRun(plugin, player, regionLocX, regionLocZ, playerLocation, logger, "home"));
        } finally {
            executor.shutdown();
        }


        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }

    private boolean isWorldIsland(String worldName) {
        return ConfigToml.worldConfigs.stream().anyMatch(wc -> wc.name().equalsIgnoreCase(worldName));
    }
}
