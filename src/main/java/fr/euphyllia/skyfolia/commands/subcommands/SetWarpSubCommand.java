package fr.euphyllia.skyfolia.commands.subcommands;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.model.Position;
import fr.euphyllia.skyfolia.commands.SubCommandInterface;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import fr.euphyllia.skyfolia.configuration.LanguageToml;
import fr.euphyllia.skyfolia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyfolia.utils.RegionUtils;
import org.apache.logging.log4j.Level;
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

public class SetWarpSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(SetWarpSubCommand.class);

    public static void setWarpRun(Main plugin, Player player, int regionLocX, int regionLocZ, Location playerLocation, Logger logger, String warpName) {
        SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
        Island island = skyblockManager.getIslandByOwner(player.getUniqueId()).join();
        if (island == null) {
            player.sendMessage(plugin.getInterneAPI().getMiniMessage().deserialize(LanguageToml.messagePlayerHasNotIsland));
            return;
        }
        Position islandPosition = island.getPosition();
        Position playerRegionPosition = RegionUtils.getRegionInChunk(regionLocX, regionLocZ);

        if (islandPosition.regionX() != playerRegionPosition.regionX() || islandPosition.regionZ() != playerRegionPosition.regionZ()) {
            player.sendMessage(plugin.getInterneAPI().getMiniMessage().deserialize(LanguageToml.messagePlayerNotInIsland));
            return;
        }

        boolean updateOrCreateWarps = island.addWarps(warpName, playerLocation);
        if (updateOrCreateWarps) {
            player.sendMessage(plugin.getInterneAPI().getMiniMessage().deserialize(LanguageToml.messageWarpCreate));
        } else {
            player.sendMessage(plugin.getInterneAPI().getMiniMessage().deserialize(LanguageToml.messageError));
        }
    }

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (args.length <= 1) {
            logger.log(Level.FATAL, "manque arguments");
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
            executor.execute(() -> SetWarpSubCommand.setWarpRun(plugin, player, regionLocX, regionLocZ, playerLocation, logger, args[0]));
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
