package fr.euphyllia.skyfolia.commands.subcommands;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.Players;
import fr.euphyllia.skyfolia.api.skyblock.model.RoleType;
import fr.euphyllia.skyfolia.commands.SubCommandInterface;
import fr.euphyllia.skyfolia.configuration.LanguageToml;
import fr.euphyllia.skyfolia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class PromoteSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(PromoteSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try {
            executor.execute(() -> {
                String playerName = args[0];
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(playerName);
                if (offlinePlayer == null) {
                    player.sendMessage(plugin.getInterneAPI().getMiniMessage().deserialize(LanguageToml.messagePlayerNotFound));
                    return;
                }

                SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
                Island island = skyblockManager.getIslandByOwner(player.getUniqueId()).join();
                if (island == null) {
                    player.sendMessage(plugin.getInterneAPI().getMiniMessage().deserialize(LanguageToml.messagePlayerHasNotIsland));
                    return;
                }
                Players players = island.getMember(offlinePlayer.getUniqueId());
                if (!players.getRoleType().equals(RoleType.MEMBER)) {
                    logger.log(Level.FATAL, "peut pas %s".formatted(players.getRoleType().name()));
                    return;
                }
                RoleType promoteResult = RoleType.getRoleById(players.getRoleType().getValue() + 1);
                players.setRoleType(promoteResult);
                island.updateMember(players);
                logger.log(Level.FATAL, "changement fait");
            });
        } finally {
            executor.shutdown();
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
