package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.PermissionManager;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.gamerule.GameRuleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameRuleSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(GameRuleSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("skyllia.island.command.gamerule")) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }
        if (args.length < 2) {
            LanguageToml.sendMessage(player, LanguageToml.messageGameRuleCommandNotEnoughArgs);
            return true;
        }
        String permissionRaw = args[0]; // Permission
        String valueRaw = args[1]; // true / false
        try {
            SkyblockManager skyblockManager = Main.getPlugin(Main.class).getInterneAPI().getSkyblockManager();
            Island island = SkylliaAPI.getCacheIslandByPlayerId(player.getUniqueId());
            if (island == null) {
                LanguageToml.sendMessage(player, LanguageToml.messagePlayerHasNotIsland);
                return true;
            }

            Players executorPlayer = island.getMember(player.getUniqueId());

            if (!executorPlayer.getRoleType().equals(RoleType.OWNER)) {
                PermissionRoleIsland permissionRoleIsland = skyblockManager.getPermissionIsland(island.getId(), PermissionsType.COMMANDS, executorPlayer.getRoleType()).join();

                PermissionManager permissionManager = new PermissionManager(permissionRoleIsland.permission());
                if (!permissionManager.hasPermission(PermissionsCommandIsland.MANAGE_GAMERULE)) {
                    LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
                    return true;
                }
            }

            GameRuleIsland gameRuleIsland;
            boolean enabledOrNot = Boolean.parseBoolean(valueRaw);
            try {
                gameRuleIsland = GameRuleIsland.valueOf(permissionRaw.toUpperCase());
            } catch (IllegalArgumentException exception) {
                LanguageToml.sendMessage(player, LanguageToml.messageGameRuleInvalid);
                return true;
            }

            long flags = island.getGameRulePermission();
            PermissionManager permissionManager = new PermissionManager(flags);
            permissionManager.definePermission(gameRuleIsland.getPermissionValue(), enabledOrNot);

            boolean updateGameRuleIsland = island.updateGamerule(permissionManager.getPermissions());

            if (updateGameRuleIsland) {
                LanguageToml.sendMessage(player, LanguageToml.messageGameRuleUpdateSuccess);
            } else {
                LanguageToml.sendMessage(player, LanguageToml.messageGameRuleUpdateFailed);
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            LanguageToml.sendMessage(player, LanguageToml.messageError);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.stream(GameRuleIsland.values()).map(Enum::name).toList();
        }
        if (args.length == 2) {
            return List.of("true", "false");
        }
        return new ArrayList<>();
    }
}