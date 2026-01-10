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
import java.util.Locale;
import java.util.Map;

public class DemoteSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(DemoteSubCommand.class);

    private final PermissionId ISLAND_DEMOTE_PERMISSION;

    public DemoteSubCommand() {
        this.ISLAND_DEMOTE_PERMISSION = SkylliaAPI.getPermissionRegistry().register(new PermissionNode(
                new NamespacedKey(Skyllia.getInstance(), "command.island.demote"),
                "Rétrograder un joueur",
                "Autorise à rétrograder un membre de l'île"
        ));
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }

        if (!player.hasPermission("skyllia.island.command.demote")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        if (args.length < 1) {
            ConfigLoader.language.sendMessage(player, "island.rank.demote-args-missing");
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
            if (executorPlayer == null) {
                ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
                return true;
            }

            boolean allowed = SkylliaAPI.getPermissionsManager().hasPermission(player, island, ISLAND_DEMOTE_PERMISSION);
            if (!allowed) {
                ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
                return true;
            }

            Players target = island.getMember(playerName);
            if (target == null) {
                ConfigLoader.language.sendMessage(player, "island.player.not-found");
                return true;
            }

            if (target.getRoleType() == RoleType.OWNER || executorPlayer.getRoleType().getValue() <= target.getRoleType().getValue()) {
                ConfigLoader.language.sendMessage(player, "island.rank.demote-high-rank");
                return true;
            }

            RoleType demoteResult = RoleType.getRoleById(target.getRoleType().getValue() - 1);
            if (demoteResult.getValue() == 0 || demoteResult.getValue() == -1) {
                ConfigLoader.language.sendMessage(player, "island.rank.demote-failed", Map.of("%s", playerName));
                return true;
            }

            target.setRoleType(demoteResult);
            boolean ok = island.updateMember(target);

            if (ok) {
                ConfigLoader.language.sendMessage(player, "island.rank.demote-success", Map.of("%s", playerName));
            } else {
                ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
            }

        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            ConfigLoader.language.sendMessage(sender, "island.generic.unexpected-error");
        }

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();
        if (!player.hasPermission("skyllia.island.command.demote")) return Collections.emptyList();

        if (args.length == 1) {
            Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
            if (island == null) return Collections.emptyList();

            String partial = args[0].trim().toLowerCase(Locale.ROOT);

            return island.getMembers().stream()
                    .filter(p -> p != null && p.getLastKnowName() != null)
                    .filter(p -> !p.getMojangId().equals(player.getUniqueId())) // pas soi-même
                    .filter(p -> p.getRoleType() != RoleType.OWNER) // jamais owner
                    .map(Players::getLastKnowName)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(partial))
                    .sorted()
                    .toList();
        }

        return Collections.emptyList();
    }
}
