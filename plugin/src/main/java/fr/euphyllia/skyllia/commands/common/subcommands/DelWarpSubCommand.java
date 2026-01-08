package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.cache.commands.CacheCommands;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DelWarpSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(DelWarpSubCommand.class);

    private final PermissionId ISLAND_DELWARP_PERMISSION;

    public DelWarpSubCommand() {
        this.ISLAND_DELWARP_PERMISSION = SkylliaAPI.getPermissionRegistry().register(new PermissionNode(
                new NamespacedKey(Skyllia.getInstance(), "command.island.delwarp"),
                "Supprimer un warp",
                "Autorise à supprimer un warp de l'île"
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

        if (!player.hasPermission("skyllia.island.command.delwarp")) {
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

            if (warpName.equalsIgnoreCase("home")) {
                ConfigLoader.language.sendMessage(player, "island.warp.delete-home-forbidden");
                return true;
            }

            boolean allowed = SkylliaAPI.getPermissionsManager().hasPermission(player, island, ISLAND_DELWARP_PERMISSION);
            if (!allowed) {
                ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
                return true;
            }

            boolean deleteWarp = island.delWarp(warpName);
            if (deleteWarp) {
                ConfigLoader.language.sendMessage(player, "island.warp.delete-success");
            } else {
                ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
        }

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (sender.hasPermission("skyllia.island.command.delwarp") && sender instanceof Player player) {
            if (args.length == 1) {
                String partial = args[0].trim().toLowerCase();
                List<String> warpList = CacheCommands.getWarps(player.getUniqueId());
                if (warpList == null || warpList.isEmpty()) return Collections.emptyList();
                return warpList.stream()
                        .filter(warp -> warp.toLowerCase().startsWith(partial))
                        .sorted()
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}
