package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.cache.commands.CacheCommands;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.PermissionsManagers;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class WarpSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(WarpSubCommand.class);

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
        if (!PermissionImp.hasPermission(sender, "skyllia.island.command.warp")) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }

        String warpName = args[0];

        try {
            SkyblockManager skyblockManager = Main.getPlugin(Main.class).getInterneAPI().getSkyblockManager();
            Island island = skyblockManager.getIslandByPlayerId(player.getUniqueId()).join();
            if (island == null) {
                LanguageToml.sendMessage(player, LanguageToml.messagePlayerHasNotIsland);
                return true;
            }

            Players executorPlayer = island.getMember(player.getUniqueId());

            if (!PermissionsManagers.testPermissions(executorPlayer, player, island, PermissionsCommandIsland.TELEPORT_WARP, false)) {
                return true;
            }

            WarpIsland warp = island.getWarpByName(warpName);
            if (warp == null) {
                LanguageToml.sendMessage(player, LanguageToml.messageWarpNotExist);
                return true;
            }

            Location warpLocation = warp.location();
            warpLocation.setY(warpLocation.getY() + 0.5);
            player.teleportAsync(warpLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
            LanguageToml.sendMessage(player, LanguageToml.messageWarpTeleportSuccess);
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            LanguageToml.sendMessage(player, LanguageToml.messageError);
        }


        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (PermissionImp.hasPermission(sender, "skyllia.island.command.warp") && sender instanceof Player player) {
            if (args.length == 1) {
                List<String> warpList = CacheCommands.warpTabCompleteCache.getIfPresent(player.getUniqueId());
                if (warpList == null || warpList.isEmpty()) return Collections.emptyList();
                String partial = args[0].trim().toLowerCase();
                return warpList.stream()
                        .filter(warp -> warp.toLowerCase().startsWith(partial))
                        .toList();
            }
        }
        return Collections.emptyList();
    }
}