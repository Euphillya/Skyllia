package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.enums.RemovalCause;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class KickSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(KickSubCommand.class);

    private final PermissionId ISLAND_KICK_PERMISSION;

    public KickSubCommand() {
        this.ISLAND_KICK_PERMISSION = SkylliaAPI.getPermissionRegistry().register(new PermissionNode(
                new NamespacedKey(Skyllia.getInstance(), "command.island.kick"),
                "Kicker un joueur",
                "Autorise à kicker un joueur de l'île"
        ));
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }
        if (!sender.hasPermission("skyllia.island.command.kick")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }
        if (args.length < 1) {
            ConfigLoader.language.sendMessage(player, "island.kick.args-missing");
            return true;
        }
        SkyblockManager skyblockManager = Skyllia.getPlugin(Skyllia.class).getInterneAPI().getSkyblockManager();
        Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "island.player.no-island");
            return true;
        }

        Players executorPlayer = island.getMember(player.getUniqueId());

        boolean allowed = SkylliaAPI.getPermissionsManager().hasPermission(player, island, ISLAND_KICK_PERMISSION);
        if (!allowed) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        String playerKick = args[0];
        Players players = island.getMember(playerKick);

        if (players == null) {
            ConfigLoader.language.sendMessage(player, "island.player.not-found");
            return true;
        }

        if (players.getRoleType().equals(RoleType.OWNER) || executorPlayer.getRoleType().getValue() <= players.getRoleType().getValue()) {
            ConfigLoader.language.sendMessage(player, "island.kick.high-rank");
            return true;
        }

        boolean isRemoved = island.removeMember(players);
        if (isRemoved) {
            ConfigLoader.language.sendMessage(player, "island.kick.success");
            DeleteSubCommand.checkClearPlayer(skyblockManager, players, RemovalCause.KICKED);
        } else {
            ConfigLoader.language.sendMessage(player, "island.kick.failed");
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
                    .sorted()
                    .toList();
        }
        return Collections.emptyList();
    }
}