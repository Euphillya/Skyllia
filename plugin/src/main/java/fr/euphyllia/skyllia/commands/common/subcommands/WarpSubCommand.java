package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.cache.commands.CacheCommands;
import fr.euphyllia.skyllia.cache.island.WarpsInIslandCache;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.managers.PermissionsManagers;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class WarpSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(WarpSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }
        if (args.length < 1) {
            ConfigLoader.language.sendMessage(player, "island.warp.args-missing");
            return true;
        }
        if (!PermissionImp.hasPermission(sender, "skyllia.island.command.warp")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        String warpName = args[0];

        try {
            SkyblockManager skyblockManager = Skyllia.getPlugin(Skyllia.class).getInterneAPI().getSkyblockManager();
            Island island = skyblockManager.getIslandByPlayerId(player.getUniqueId()).join();
            if (island == null) {
                ConfigLoader.language.sendMessage(player, "island.player.no-island");
                return true;
            }

            Players executorPlayer = island.getMember(player.getUniqueId());
            if (!PermissionsManagers.testPermissions(executorPlayer, player, island, PermissionsCommandIsland.TELEPORT_WARP, false)) {
                return true;
            }

            UUID islandId = island.getId();
            CopyOnWriteArrayList<WarpIsland> warps = WarpsInIslandCache.getWarpsCached(islandId);

            WarpIsland targetWarp = null;
            for (WarpIsland warp : warps) {
                if (warp.warpName().equalsIgnoreCase(warpName)) {
                    targetWarp = warp;
                    break;
                }
            }

            if (targetWarp == null) {
                ConfigLoader.language.sendMessage(player, "island.warp.warp-not-exist");
                return true;
            }

            player.teleportAsync(targetWarp.location(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            ConfigLoader.language.sendMessage(player, "island.warp.teleport-success");
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
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