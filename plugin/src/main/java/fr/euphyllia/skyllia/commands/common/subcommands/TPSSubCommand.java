package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.utils.TPSFormatter;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class TPSSubCommand implements SubCommandInterface {
    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }
        if (!PermissionImp.hasPermission(sender, "skyllia.island.command.tps")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        final Location playerLocation = player.getLocation();
        if (!WorldUtils.isWorldSkyblock(playerLocation.getWorld().getName())) {
            ConfigLoader.language.sendMessage(player, "island.player.not-on-island");
            return true;
        }
        Bukkit.getRegionScheduler().run(plugin, playerLocation, (task) -> {
            double @Nullable [] tpsIsland = SkylliaAPI.getTPS(playerLocation);
            if (tpsIsland == null) {
                ConfigLoader.language.sendMessage(player, "island.player.not-on-island");
                return;
            }
            double @Nullable [] msptIsland = SkylliaAPI.getAverageTickTime(playerLocation);
            if (msptIsland == null) {
                ConfigLoader.language.sendMessage(player, "island.player.not-on-island");
                return;
            }
            player.sendMessage(TPSFormatter.displayTPS(tpsIsland, msptIsland).asComponent());
        });
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
