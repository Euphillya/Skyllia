package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class UnbanSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(UnbanSubCommand.class);

    private final PermissionId ISLAND_UNBAN_PERMISSION;

    public UnbanSubCommand() {
        this.ISLAND_UNBAN_PERMISSION = SkylliaAPI.getPermissionRegistry().register(new PermissionNode(
                new NamespacedKey(Skyllia.getInstance(), "command.island.unban"),
                "Débannir un joueur",
                "Autorise à débannir un joueur de l'île"
        ));
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }

        if (!player.hasPermission("skyllia.island.command.unban")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        if (args.length < 1) {
            ConfigLoader.language.sendMessage(player, "island.unban.not-enough-args");
            return true;
        }

        Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "island.player.no-island");
            return true;
        }

        boolean allowed = SkylliaAPI.getPermissionsManager().hasPermission(player, island, ISLAND_UNBAN_PERMISSION);
        if (!allowed) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        String playerBan = args[0];
        Players players = island.getMember(playerBan);

        if (players == null) {
            ConfigLoader.language.sendMessage(player, "island.unban.player-not-banned");
            return true;
        }

        boolean isRemoved = island.removeMember(players);
        if (isRemoved) {
            ConfigLoader.language.sendMessage(player, "island.unban.success");
        } else {
            ConfigLoader.language.sendMessage(player, "island.unban.failed");
        }
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}