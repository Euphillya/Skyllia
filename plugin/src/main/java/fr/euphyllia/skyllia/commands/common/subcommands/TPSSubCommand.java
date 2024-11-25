package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.utils.TPSFormatter;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TPSSubCommand implements SubCommandInterface {
    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            LanguageToml.sendMessage(sender, LanguageToml.messageCommandPlayerOnly);
            return true;
        }
        if (!player.hasPermission("skyllia.island.command.tps")) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }

        Location playerLocation = player.getLocation();
        if (Boolean.FALSE.equals(WorldUtils.isWorldSkyblock(playerLocation.getWorld().getName()))) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerIsNotOnAnIsland);
            return true;
        }

        double @Nullable [] tpsIsland = SkylliaAPI.getTPS(playerLocation);
        if (tpsIsland == null) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerIsNotOnAnIsland);
            return true;
        }
        player.sendMessage(TPSFormatter.displayTPS(tpsIsland).asComponent());
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
