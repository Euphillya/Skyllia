package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.PermissionsManagers;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SetWarpSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(SetWarpSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            LanguageToml.sendMessage(sender, LanguageToml.messageCommandPlayerOnly);
            return true;
        }
        if (args.length < 1) {
            LanguageToml.sendMessage(player, LanguageToml.messageWarpCommandNotEnoughArgs);
            return true;
        }
        if (!PermissionImp.hasPermission(player, "skyllia.island.command.setwarp")) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }

        Location playerLocation = player.getLocation();
        if (Boolean.FALSE.equals(WorldUtils.isWorldSkyblock(playerLocation.getWorld().getName()))) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerIsNotOnAnIsland);
            return true;
        }

        String warpName = args[0];

        SkyblockManager skyblockManager = Main.getPlugin(Main.class).getInterneAPI().getSkyblockManager();
        Island island = skyblockManager.getIslandByPlayerId(player.getUniqueId()).join();
        if (island == null) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerHasNotIsland);
            return true;
        }

        Players executorPlayer = island.getMember(player.getUniqueId());

        if (!PermissionsManagers.testPermissions(executorPlayer, player, island, PermissionsCommandIsland.SET_WARP, false)) {
            return true;
        }

        Position islandPosition = island.getPosition();


        try {
            player.getScheduler().run(plugin, aScheduled -> {
                int regionLocX = playerLocation.getChunk().getX();
                int regionLocZ = playerLocation.getChunk().getZ();
                Position playerRegionPosition = RegionHelper.getRegionFromChunk(regionLocX, regionLocZ);

                if (islandPosition.x() != playerRegionPosition.x() || islandPosition.z() != playerRegionPosition.z()) {
                    LanguageToml.sendMessage(player, LanguageToml.messagePlayerNotInIsland);
                    return;
                }

                Bukkit.getAsyncScheduler().runNow(plugin, aScheduler -> {
                    boolean updateOrCreateWarps = island.addWarps(warpName, playerLocation, false);
                    if (updateOrCreateWarps) {
                        LanguageToml.sendMessage(player, LanguageToml.messageWarpCreateSuccess.formatted(warpName));
                    } else {
                        LanguageToml.sendMessage(player, LanguageToml.messageError);
                    }
                });
            }, null);
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            LanguageToml.sendMessage(player, LanguageToml.messageError);
        }

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
