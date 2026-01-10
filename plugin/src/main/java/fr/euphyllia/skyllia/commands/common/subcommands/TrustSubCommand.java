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
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class TrustSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(TrustSubCommand.class);

    private final PermissionId ISLAND_MANAGE_TRUST_PERMISSION;

    public TrustSubCommand() {
        this.ISLAND_MANAGE_TRUST_PERMISSION = SkylliaAPI.getPermissionRegistry().register(
                new PermissionNode(
                        new NamespacedKey(Skyllia.getInstance(), "command.island.manage_trust"),
                        "Gérer les accès de confiance",
                        "Autorise à ajouter des joueurs en trusted sur l'île"
                )
        );
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }

        if (!player.hasPermission("skyllia.island.command.access")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        if (args.length < 1) {
            ConfigLoader.language.sendMessage(player, "island.trust.args-missing");
            return true;
        }

        try {
            String targetName = args[0];

            UUID targetId = Bukkit.getPlayerUniqueId(targetName);
            if (targetId == null) {
                ConfigLoader.language.sendMessage(player, "island.player.not-found");
                return true;
            }

            Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
            if (island == null) {
                ConfigLoader.language.sendMessage(player, "island.player.no-island");
                return true;
            }

            boolean allowed = SkylliaAPI.getPermissionsManager()
                    .hasPermission(player, island, ISLAND_MANAGE_TRUST_PERMISSION);

            if (!allowed) {
                ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
                return true;
            }

            boolean added = Skyllia.getInstance()
                    .getInterneAPI()
                    .getTrustService()
                    .addTrusted(island.getId(), targetId);

            if (added) {
                ConfigLoader.language.sendMessage(player, "island.trust.success", Map.of("%trusted_name%", targetName));
            } else {
                ConfigLoader.language.sendMessage(player, "island.trust.already-trusted", Map.of("%trusted_name%", targetName));
            }

            return true;

        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
            return true;
        }
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
