package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.PermissionsManagers;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class DebugSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(DebugSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!PermissionImp.hasPermission(player, "skyllia.island.command.debug")) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }

        if (args.length < 1) {
            LanguageToml.sendMessage(player, LanguageToml.messageDebugCommandNotEnoughArgs);
            return true;
        }

        SkyblockManager skyblockManager = Main.getPlugin(Main.class).getInterneAPI().getSkyblockManager();
        Island island = skyblockManager.getIslandByPlayerId(player.getUniqueId()).join();
        if (island == null) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerHasNotIsland);
            return true;
        }

        Players executorPlayer = island.getMember(player.getUniqueId());

        if (!PermissionsManagers.testPermissions(executorPlayer, player, island, PermissionsCommandIsland.DEBUG, false)) {
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "gamerule": {
                PermissionsManagers.toggleDebug(player.getUniqueId(), PermissionsManagers.DebugType.GAME_RULE);
            }
            case "island": {
                PermissionsManagers.toggleDebug(player.getUniqueId(), PermissionsManagers.DebugType.ISLAND_PERMISSION);
            }
            case "commands": {
                PermissionsManagers.toggleDebug(player.getUniqueId(), PermissionsManagers.DebugType.COMMANDS_PERMISSION);
            }
            case "inventory": {
                PermissionsManagers.toggleDebug(player.getUniqueId(), PermissionsManagers.DebugType.INVENTORY_PERMISSION);
            }
        }

        LanguageToml.sendMessage(player, "Debug " + args[0] + " enabled");


        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();
            Set<String> commands = Set.of("gamerule", "island", "commands", "inventory");

            return commands.stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(partial))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
