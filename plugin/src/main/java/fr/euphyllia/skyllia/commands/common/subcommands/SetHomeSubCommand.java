package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.managers.PermissionsManagers;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
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

public class SetHomeSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(SetHomeSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }
        if (!PermissionImp.hasPermission(sender, "skyllia.island.command.sethome")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        SkyblockManager skyblockManager = Skyllia.getPlugin(Skyllia.class).getInterneAPI().getSkyblockManager();
        Island island = skyblockManager.getIslandByPlayerId(player.getUniqueId()).join();
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "island.player.no-island");
            return true;
        }

        Position islandPosition = island.getPosition();

        Players executorPlayer = island.getMember(player.getUniqueId());

        if (!PermissionsManagers.testPermissions(executorPlayer, player, island, PermissionsCommandIsland.SET_HOME, false)) {
            return true;
        }

        player.getScheduler().run(plugin, pScheduler -> {
            Location playerLocation = player.getLocation();
            int regionLocX = playerLocation.getChunk().getX();
            int regionLocZ = playerLocation.getChunk().getZ();

            try {
                Position playerRegionPosition = RegionHelper.getRegionFromChunk(regionLocX, regionLocZ);
                if (islandPosition.x() != playerRegionPosition.x() || islandPosition.z() != playerRegionPosition.z()) {
                    ConfigLoader.language.sendMessage(player, "island.player.not-on-own-island");
                    return;
                }

                Bukkit.getAsyncScheduler().runNow(plugin, aScheduler -> {
                    boolean updateOrCreateHome = island.addWarps("home", playerLocation, false);
                    if (updateOrCreateHome) {
                        ConfigLoader.language.sendMessage(player, "island.home.set.success");
                    } else {
                        ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
                    }
                });
            } catch (Exception e) {
                logger.log(Level.FATAL, e.getMessage(), e);
                ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
            }
        }, null);

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
