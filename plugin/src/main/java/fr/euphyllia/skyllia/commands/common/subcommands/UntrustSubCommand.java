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
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class UntrustSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(UntrustSubCommand.class);

    private final PermissionId ISLAND_MANAGE_TRUST_PERMISSION;

    public UntrustSubCommand() {
        this.ISLAND_MANAGE_TRUST_PERMISSION = SkylliaAPI.getPermissionRegistry().register(
                new PermissionNode(
                        new NamespacedKey(Skyllia.getInstance(), "command.island.manage_trust"),
                        "Gérer les accès de confiance",
                        "Autorise à retirer des joueurs trusted de l'île"
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
            ConfigLoader.language.sendMessage(player, "island.untrust.args-missing");
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

            boolean removed = Skyllia.getInstance()
                    .getInterneAPI()
                    .getTrustService()
                    .removeTrusted(island.getId(), targetId);

            if (removed) {
                ConfigLoader.language.sendMessage(player, "island.untrust.success", Map.of("%trusted_name", targetName));
            } else {
                ConfigLoader.language.sendMessage(player, "island.untrust.failed", Map.of("%trusted_name%", targetName));
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
        if (!(sender instanceof Player player)) {
            return List.of();
        }

        if (args.length != 1) {
            return List.of();
        }

        Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
        if (island == null) {
            return List.of();
        }

        Set<UUID> trusted = Skyllia.getInstance()
                .getInterneAPI()
                .getTrustService()
                .getTrusted(island.getId());

        if (trusted == null || trusted.isEmpty()) {
            return List.of();
        }

        String prefix = args[0].toLowerCase();

        return trusted.stream()
                .map(Bukkit::getOfflinePlayer)
                .map(OfflinePlayer::getName)
                .filter(name -> name != null && name.toLowerCase().startsWith(prefix))
                .sorted()
                .collect(Collectors.toList());
    }
}
