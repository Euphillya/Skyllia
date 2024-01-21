package fr.euphyllia.skyllia.commands.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.commands.SubCommandInterface;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

// Todo ? Pas essayer
public class TransferSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(TransferSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if (!(sender instanceof Player player)) {
                return true;
            }
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            try {
                executor.execute(() -> {
                    SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
                    Island island = skyblockManager.getIslandByOwner(player.getUniqueId()).join();

                    if (island == null) {
                        player.sendMessage(plugin.getInterneAPI().getMiniMessage().deserialize(LanguageToml.messagePlayerHasNotIsland));
                        return;
                    }
                    if (!island.getOwnerId().equals(player.getUniqueId())) {
                        player.sendMessage(plugin.getInterneAPI().getMiniMessage().deserialize(LanguageToml.messageOnlyOwner));
                        return;
                    }

                    String newOwner = args[0];
                    Players players = island.getMember(newOwner);
                    if (players == null || !players.getRoleType().equals(RoleType.MEMBER) || !players.getRoleType().equals(RoleType.MODERATOR)) {
                        player.sendMessage(plugin.getInterneAPI().getMiniMessage().deserialize(LanguageToml.messageNotMember));
                        return;
                    }

                    // Enlever le proprio
                    Players oldOwner = island.getMember(player.getUniqueId());
                    oldOwner.setRoleType(RoleType.MEMBER);
                    island.updateMember(oldOwner);
                    // Nouveau proprio
                    island.setOwnerId(player.getUniqueId());
                    players.setRoleType(RoleType.OWNER);
                    island.updateMember(players);
                    player.sendMessage(plugin.getInterneAPI().getMiniMessage().deserialize(LanguageToml.messageTransfertSuccess.replace("%new_owner%", players.getLastKnowName())));
                    // msg ok
                });
            } finally {
                executor.shutdown();
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, "", e);
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
