package fr.euphyllia.skyllia.commands.admin.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.event.SkyblockChangeOwnerEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ForceTransferSubCommands implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(ForceTransferSubCommands.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("skyllia.admins.commands.island.transfer")) {
            LanguageToml.sendMessage(sender, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }
        if (args.length < 2) {
            LanguageToml.sendMessage(sender, LanguageToml.messageATransferCommandNotEnoughArgs);
            return true;
        }

        String previousOwnerName = args[0];
        String newOwnerName = args[1];
        String confirm = args.length >= 3 ? args[2] : "";

        if (!confirm.equalsIgnoreCase("confirm")) {
            LanguageToml.sendMessage(sender, LanguageToml.messageATransferNotConfirmedArgs);
            return true;
        }

        try {
            UUID previousOwnerId;
            try {
                previousOwnerId = UUID.fromString(previousOwnerName);
            } catch (IllegalArgumentException ignored) {
                previousOwnerId = Bukkit.getPlayerUniqueId(previousOwnerName);
            }

            UUID newOwnerId;
            try {
                newOwnerId = UUID.fromString(newOwnerName);
            } catch (IllegalArgumentException ignored) {
                newOwnerId = Bukkit.getPlayerUniqueId(newOwnerName);
            }

            SkyblockManager skyblockManager = Main.getPlugin(Main.class).getInterneAPI().getSkyblockManager();
            Island island = skyblockManager.getIslandByOwner(previousOwnerId).join();

            if (island == null) {
                LanguageToml.sendMessage(sender, LanguageToml.messagePlayerHasNotIsland);
                return true;
            }

            Players oldOwner = skyblockManager.getOwnerByIslandID(island).join();
            if (oldOwner == null || !oldOwner.getMojangId().equals(previousOwnerId)) {
                LanguageToml.sendMessage(sender, LanguageToml.messageOnlyOwner);
                return true;
            }

            Players newOwner = island.getMember(newOwnerName);
            if (newOwner == null) {
                LanguageToml.sendMessage(sender, LanguageToml.messagePlayerIsNotOnAnIsland);
                return true;
            }

            if (newOwner.getMojangId().equals(previousOwnerId)) {
                LanguageToml.sendMessage(sender, LanguageToml.messageTransfertAlreadyOwner);
                return true;
            }

            // Transfert de propriété
            oldOwner.setRoleType(RoleType.CO_OWNER);
            island.updateMember(oldOwner);

            newOwner.setRoleType(RoleType.OWNER);
            island.updateMember(newOwner);

            // Déclencher l'événement de changement de propriétaire
            SkyblockChangeOwnerEvent event = new SkyblockChangeOwnerEvent(island, oldOwner.getMojangId(), newOwner.getMojangId());
            Bukkit.getPluginManager().callEvent(event);

            LanguageToml.sendMessage(sender, LanguageToml.messageTransfertSuccess
                    .replace("%new_owner%", newOwner.getLastKnowName())
                    .replace("%old_owner%", oldOwner.getLastKnowName()));

            Player newOwnerPlayer = plugin.getServer().getPlayer(newOwner.getMojangId());
            if (newOwnerPlayer != null && newOwnerPlayer.isOnline()) {
                LanguageToml.sendMessage(newOwnerPlayer, LanguageToml.messageTransfertSuccessOldOwnerNotification
                        .replace("%old_owner%", oldOwner.getLastKnowName()));
            }

        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            LanguageToml.sendMessage(sender, LanguageToml.messageError);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("skyllia.admins.commands.island.transfer")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            return List.of("confirm");
        }

        return new ArrayList<>();
    }
}
