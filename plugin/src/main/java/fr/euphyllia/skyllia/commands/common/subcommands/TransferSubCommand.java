package fr.euphyllia.skyllia.commands.common.subcommands;

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

public class TransferSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(TransferSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if (!(sender instanceof Player player)) {
                return true;
            }
            SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
            Island island = skyblockManager.getIslandByOwner(player.getUniqueId()).join();

            if (island == null) {
                player.sendMessage(plugin.getInterneAPI().getMiniMessage().deserialize(LanguageToml.messagePlayerHasNotIsland));
                return true;
            }
            Players ownerIsland = skyblockManager.getOwnerByIslandID(island).join();
            if (ownerIsland == null) {
                LanguageToml.sendMessage(player, LanguageToml.messageError);
                return true;
            }
            if (!ownerIsland.getMojangId().equals(player.getUniqueId())) {
                LanguageToml.sendMessage(player, LanguageToml.messageOnlyOwner);
                return true;
            }

            String newOwner = args[0];
            Players players = island.getMember(newOwner);
            if (players == null || !players.getRoleType().equals(RoleType.MEMBER) || !players.getRoleType().equals(RoleType.MODERATOR)) {
                LanguageToml.sendMessage(player, LanguageToml.messageNotMember);
                return true;
            }

            // Enlever le proprio
            Players oldOwner = island.getMember(player.getUniqueId());
            oldOwner.setRoleType(RoleType.MEMBER);
            island.updateMember(oldOwner);
            // Nouveau proprio
            players.setRoleType(RoleType.OWNER);
            island.updateMember(players);
            LanguageToml.sendMessage(player, LanguageToml.messageTransfertSuccess.replace("%new_owner%", players.getLastKnowName()));
            // msg ok
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
