package fr.euphyllia.skyllia.commands.admin.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.event.SkyblockChangeOwnerEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ForceTransferSubCommands implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(ForceTransferSubCommands.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("skyllia.admins.commands.island.transfer")) {
            ConfigLoader.language.sendMessage(sender, "island.player.permission-denied");
            return true;
        }
        if (args.length < 2) {
            ConfigLoader.language.sendMessage(sender, "island.admin.transfer-args-missing");
            return true;
        }

        String previousOwnerName = args[0];
        String newOwnerName = args[1];
        String confirm = args.length >= 3 ? args[2] : "";

        if (!confirm.equalsIgnoreCase("confirm")) {
            ConfigLoader.language.sendMessage(sender, "island.admin.transfer-no-confirm");
            return true;
        }

        try {
            UUID previousOwnerId;
            try {
                previousOwnerId = UUID.fromString(previousOwnerName);
            } catch (IllegalArgumentException ignored) {
                previousOwnerId = Bukkit.getPlayerUniqueId(previousOwnerName);
            }

            SkyblockManager skyblockManager = Skyllia.getPlugin(Skyllia.class).getInterneAPI().getSkyblockManager();
            Island island = skyblockManager.getIslandByOwner(previousOwnerId);

            if (island == null) {
                ConfigLoader.language.sendMessage(sender, "island.player.no-island");
                return true;
            }

            Players oldOwner = skyblockManager.getOwnerByIslandID(island);
            if (oldOwner == null || !oldOwner.getMojangId().equals(previousOwnerId)) {
                ConfigLoader.language.sendMessage(sender, "island.only-owner");
                return true;
            }

            Players newOwner = island.getMember(newOwnerName);
            if (newOwner == null) {
                ConfigLoader.language.sendMessage(sender, "island.player.not-on-island");
                return true;
            }

            if (newOwner.getMojangId().equals(previousOwnerId)) {
                ConfigLoader.language.sendMessage(sender, "island.transfer.already-owner");
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

            ConfigLoader.language.sendMessage(sender, "island.transfer.success", Map.of(
                    "%new_owner%", newOwner.getLastKnowName(),
                    "%old_owner%", oldOwner.getLastKnowName()));

            Player newOwnerPlayer = plugin.getServer().getPlayer(newOwner.getMojangId());
            if (newOwnerPlayer != null && newOwnerPlayer.isOnline()) {
                ConfigLoader.language.sendMessage(newOwnerPlayer, "island.transfer.notify-old-owner", Map.of(
                        "%new_owner%", newOwner.getLastKnowName(),
                        "%old_owner%", oldOwner.getLastKnowName()));
            }

        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            ConfigLoader.language.sendMessage(sender, "island.generic.unexpected-error");
        }

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("skyllia.admins.commands.island.transfer")) {
            return Collections.emptyList();
        }

        // ARG 1 → Nom du premier joueur
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();
            return new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .sorted()
                    .collect(Collectors.toList());
        }

        // ARG 2 → Nom du deuxième joueur
        if (args.length == 2) {
            String partial = args[1].trim().toLowerCase();
            return new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .sorted()
                    .collect(Collectors.toList());
        }

        // ARG 3 → Saisie pour "confirm"
        if (args.length == 3) {
            String partial = args[2].trim().toLowerCase();
            return Stream.of("confirm")
                    .filter(word -> word.startsWith(partial))
                    .collect(Collectors.toList());
        }

        // Aucun autre argument
        return Collections.emptyList();
    }
}
