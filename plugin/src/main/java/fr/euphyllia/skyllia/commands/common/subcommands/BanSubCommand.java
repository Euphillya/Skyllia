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
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BanSubCommand implements SubCommandInterface {

    private final PermissionId ISLAND_BAN_PERMISSION;

    public BanSubCommand() {
        this.ISLAND_BAN_PERMISSION = SkylliaAPI.getPermissionRegistry().register(new PermissionNode(
                new NamespacedKey(Skyllia.getInstance(), "command.island.ban"),
                "Bannir un joueur",
                "Autorise à bannir un joueur de l'île"
        ));
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }

        if (!player.hasPermission("skyllia.island.command.ban")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        if (args.length < 1) {
            ConfigLoader.language.sendMessage(player, "island.ban.args-missing");
            return true;
        }

        Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "island.player.no-island");
            return true;
        }

        boolean allowed = SkylliaAPI.getPermissionsManager().hasPermission(player, island, ISLAND_BAN_PERMISSION);
        if (!allowed) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        final String targetName = args[0];

        Players existing = island.getMember(targetName);
        if (existing != null) {
            if (existing.getRoleType() == RoleType.BAN) {
                ConfigLoader.language.sendMessage(player, "island.ban.already-banned");
            } else {
                ConfigLoader.language.sendMessage(player, "island.ban.failed-player-in-island");
            }
            return true;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            ConfigLoader.language.sendMessage(player, "island.player.not-found");
            return true;
        }

        Players banned = new Players(target.getUniqueId(), targetName, island.getId(), RoleType.BAN);

        island.updateMember(banned);
        ConfigLoader.language.sendMessage(player, "island.ban.success");

        ExpelSubCommand.expelPlayer(Skyllia.getPlugin(Skyllia.class), island, target, player, true);
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();
            var onlinePlayers = Bukkit.getOnlinePlayers();
            return onlinePlayers.stream()
                    .map(CommandSender::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
