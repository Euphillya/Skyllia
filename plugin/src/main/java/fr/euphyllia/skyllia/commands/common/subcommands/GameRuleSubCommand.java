package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.skyblock.Island;
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
import java.util.stream.Stream;

public class GameRuleSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(GameRuleSubCommand.class);

    private final PermissionId ISLAND_GAMERULE_PERMISSION;

    public GameRuleSubCommand() {
        this.ISLAND_GAMERULE_PERMISSION = SkylliaAPI.getPermissionRegistry().register(new PermissionNode(
                new NamespacedKey(Skyllia.getInstance(), "command.island.gamerule"),
                "Gérer les gamerules",
                "Autorise à modifier les gamerules de l'île"
        ));
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }

        if (!player.hasPermission("skyllia.island.command.gamerule")) {
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
            Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
            if (island == null) {
                ConfigLoader.language.sendMessage(player, "island.player.no-island");
                return true;
            }

            boolean allowed = SkylliaAPI.getPermissionsManager()
                    .hasPermission(player, island, ISLAND_GAMERULE_PERMISSION);

            if (!allowed) {
                ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
                return true;
            }
            // Todo : Systeme de gamerule fusionner avec les permissions
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

//            return Arrays.stream(GameRuleIsland.values())
//                    .map(Enum::name)
//                    .filter(name -> name.toLowerCase().startsWith(partial))
//                    .toList();
        }
        if (args.length == 2) {
            String partial = args[1].trim().toLowerCase();
            return Stream.of("true", "false").filter(value -> value.startsWith(partial)).toList();
        }

        return Collections.emptyList();
    }
}