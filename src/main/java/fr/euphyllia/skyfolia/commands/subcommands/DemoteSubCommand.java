package fr.euphyllia.skyfolia.commands.subcommands;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.Players;
import fr.euphyllia.skyfolia.api.skyblock.model.RoleType;
import fr.euphyllia.skyfolia.commands.SubCommandInterface;
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

public class DemoteSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(DemoteSubCommand.class);

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
                    player.sendMessage( "Le joueur est introuvable.");
                    return;
                }

                SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
                Island island = skyblockManager.getIslandByOwner(player).join();
                if (island == null) {
                    return;
                }
                RoleType rolePlayers = skyblockManager.getRoleTypePlayer(island, offlinePlayer).join();
                if (rolePlayers.equals(RoleType.BAN) || rolePlayers.equals(RoleType.MEMBER)) {
                    logger.log(Level.FATAL, "peut pas %s".formatted(rolePlayers.name()));
                    return;
                }
                RoleType demoteResult = RoleType.getRoleById(rolePlayers.getValue() - 1);
                Players players = new Players(offlinePlayer.getUniqueId(), offlinePlayer.getPlayerProfile().getName(), island.getIslandId(), demoteResult);
                island.updateMember(players, demoteResult);
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
