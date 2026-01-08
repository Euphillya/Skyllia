package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.cache.commands.CacheCommands;
import fr.euphyllia.skyllia.cache.island.WarpsInIslandCache;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class WarpSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(WarpSubCommand.class);

    private final PermissionId ISLAND_WARP_PERMISSION;

    public WarpSubCommand() {
        this.ISLAND_WARP_PERMISSION = SkylliaAPI.getPermissionRegistry().register(new PermissionNode(
                new NamespacedKey(Skyllia.getInstance(), "command.island.warp"),
                "Se téléporter à un warp",
                "Autorise à utiliser /is warp sur l'île"
        ));
    }


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

        if (!player.hasPermission("skyllia.island.command.warp")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        String warpName = args[0];

        try {

            Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
            if (island == null) {
                ConfigLoader.language.sendMessage(player, "island.player.no-island");
                return true;
            }

            boolean allowed = SkylliaAPI.getPermissionsManager().hasPermission(player, island, ISLAND_WARP_PERMISSION);
            if (!allowed) {
                ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
                return true;
            }

            UUID islandId = island.getId();
            List<WarpIsland> warps = WarpsInIslandCache.getWarpsCached(islandId);

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
        if (sender instanceof Player player && sender.hasPermission("skyllia.island.command.warp")) {
            if (args.length == 1) {
                List<String> warpList = CacheCommands.getWarps(player.getUniqueId());
                if (warpList == null || warpList.isEmpty()) {
                    return Collections.emptyList();
                }
                String partial = args[0].trim().toLowerCase();
                return warpList.stream()
                        .filter(warp -> warp.toLowerCase().startsWith(partial))
                        .toList();
            }
        }
        return Collections.emptyList();
    }
}