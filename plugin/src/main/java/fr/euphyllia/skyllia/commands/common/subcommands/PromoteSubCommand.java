package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
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
import java.util.Map;

public class PromoteSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(PromoteSubCommand.class);

    private final PermissionId ISLAND_PROMOTE_PERMISSION;

    public PromoteSubCommand() {
        this.ISLAND_PROMOTE_PERMISSION = SkylliaAPI.getPermissionRegistry().register(new PermissionNode(
                new NamespacedKey(Skyllia.getInstance(), "command.island.promote"),
                "Promouvoir un joueur",
                "Autorise à promouvoir un membre de l'île"
        ));
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }

        if (!player.hasPermission("skyllia.island.command.promote")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        if (args.length < 1) {
            ConfigLoader.language.sendMessage(player, "island.rank.promote-args-missing");
            return true;
        }
        try {
            String playerName = args[0];

            Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
            if (island == null) {
                ConfigLoader.language.sendMessage(player, "island.player.no-island");
                return true;
            }

            Players executorPlayer = island.getMember(player.getUniqueId());

            boolean allowed = SkylliaAPI.getPermissionsManager().hasPermission(player, island, ISLAND_PROMOTE_PERMISSION);
            if (!allowed) {
                ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
                return true;
            }

            Players players = island.getMember(playerName);

            if (players == null) {
                ConfigLoader.language.sendMessage(player, "island.player.not-found");
                return true;
            }

            if (executorPlayer.getRoleType().getValue() <= players.getRoleType().getValue()) {
                ConfigLoader.language.sendMessage(player, "island.rank.promote-high-rank");
                return true;
            }

            RoleType promoteResult = RoleType.getRoleById(players.getRoleType().getValue() + 1);
            if (promoteResult.getValue() == 0 || promoteResult.getValue() == RoleType.OWNER.getValue()) {
                ConfigLoader.language.sendMessage(player, "island.rank.promote-failed", Map.of(
                        "%s", playerName));
                return true;
            }
            players.setRoleType(promoteResult);
            island.updateMember(players);
            ConfigLoader.language.sendMessage(player, "island.admin.rank.promote-success", Map.of(
                    "%s", playerName));
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
        }

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            String partial = args[0].trim().toLowerCase();
            Island island = SkylliaAPI.getCacheIslandByPlayerId(player.getUniqueId());
            if (island == null) return Collections.emptyList();
            return island.getMembersCached().stream()
                    .map(Players::getLastKnowName)
                    .filter(cmd -> cmd.toLowerCase().startsWith(partial))
                    .toList();
        }
        return Collections.emptyList();
    }
}
