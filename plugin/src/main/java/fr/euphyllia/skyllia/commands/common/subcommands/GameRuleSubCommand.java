package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.PermissionManager;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.gamerule.GameRuleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.managers.PermissionsManagers;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class GameRuleSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(GameRuleSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }
        if (!PermissionImp.hasPermission(sender, "skyllia.island.command.gamerule")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }
        if (args.length < 2) {
            ConfigLoader.language.sendMessage(player, "island.gamerule.args-missing");
            return true;
        }
        String permissionRaw = args[0]; // Permission
        String valueRaw = args[1]; // true / false
        try {
            SkyblockManager skyblockManager = Main.getPlugin(Main.class).getInterneAPI().getSkyblockManager();
            Island island = skyblockManager.getIslandByPlayerId(player.getUniqueId()).join();
            if (island == null) {
                ConfigLoader.language.sendMessage(player, "island.player.no-island");
                return true;
            }

            Players executorPlayer = island.getMember(player.getUniqueId());

            if (!PermissionsManagers.testPermissions(executorPlayer, player, island, PermissionsCommandIsland.MANAGE_GAMERULE, false)) {
                return true;
            }

            GameRuleIsland gameRuleIsland;
            boolean enabledOrNot = Boolean.parseBoolean(valueRaw);
            try {
                gameRuleIsland = GameRuleIsland.valueOf(permissionRaw.toUpperCase());
            } catch (IllegalArgumentException exception) {
                ConfigLoader.language.sendMessage(player, "island.gamerule.invalid");
                return true;
            }

            long flags = island.getGameRulePermission();
            PermissionManager permissionManager = new PermissionManager(flags);
            permissionManager.definePermission(gameRuleIsland.getPermissionValue(), enabledOrNot);

            boolean updateGameRuleIsland = island.updateGamerule(permissionManager.getPermissions());

            if (updateGameRuleIsland) {
                ConfigLoader.language.sendMessage(player, "island.gamerule.success");
            } else {
                ConfigLoader.language.sendMessage(player, "island.gamerule.failed");
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
        }
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();

            return Arrays.stream(GameRuleIsland.values())
                    .map(Enum::name)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .toList();
        }
        if (args.length == 2) {
            String partial = args[1].trim().toLowerCase();
            return Stream.of("true", "false").filter(value -> value.startsWith(partial)).toList();
        }

        return Collections.emptyList();
    }
}