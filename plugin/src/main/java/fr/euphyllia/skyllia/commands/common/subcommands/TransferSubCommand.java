package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.event.SkyblockChangeOwnerEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class TransferSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(TransferSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            LanguageToml.sendMessage(sender, LanguageToml.messageCommandPlayerOnly);
            return true;
        }

        if (args.length == 0) {
            LanguageToml.sendMessage(player, LanguageToml.messageTransfertSpecifyPlayer);
            return true;
        }

        if (!player.hasPermission("skyllia.island.command.transfer")) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }

        String newOwnerName = args[0];

        boolean isConfirm = args.length >= 2 && args[1].equalsIgnoreCase("confirm");

        if (!isConfirm) {
            LanguageToml.sendMessage(player, LanguageToml.messageTransfertConfirmation
                    .replace("%new_owner%", newOwnerName));
            return true;
        }

        SkyblockManager skyblockManager = Main.getPlugin(Main.class).getInterneAPI().getSkyblockManager();
        Island island = skyblockManager.getIslandByOwner(player.getUniqueId()).join();

        if (island == null) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerHasNotIsland);
            return true;
        }

        Players ownerIsland = skyblockManager.getOwnerByIslandID(island).join();

        if (ownerIsland == null || !ownerIsland.getMojangId().equals(player.getUniqueId())) {
            LanguageToml.sendMessage(player, LanguageToml.messageOnlyOwner);
            return true;
        }

        Players newOwner = island.getMember(newOwnerName);

        if (newOwner == null) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerIsNotOnAnIsland);
            return true;
        }

        if (newOwner.getMojangId().equals(player.getUniqueId())) {
            LanguageToml.sendMessage(player, LanguageToml.messageTransfertAlreadyOwner);
            return true;
        }

        if (!newOwner.getRoleType().equals(RoleType.CO_OWNER)) {
            LanguageToml.sendMessage(player, LanguageToml.messageTransfertOnlyCoOwner);
            return true;
        }

        Players oldOwner = island.getMember(player.getUniqueId());
        oldOwner.setRoleType(RoleType.CO_OWNER);
        island.updateMember(oldOwner);

        newOwner.setRoleType(RoleType.OWNER);
        island.updateMember(newOwner);

        SkyblockChangeOwnerEvent event = new SkyblockChangeOwnerEvent(island, oldOwner.getMojangId(), newOwner.getMojangId());
        Bukkit.getPluginManager().callEvent(event);

        LanguageToml.sendMessage(player, LanguageToml.messageTransfertSuccess.replace("%new_owner%", newOwner.getLastKnowName()));

        Player newOwnerPlayer = plugin.getServer().getPlayer(newOwner.getMojangId());
        if (newOwnerPlayer != null && newOwnerPlayer.isOnline()) {
            LanguageToml.sendMessage(newOwnerPlayer, LanguageToml.messageTransfertSuccessOldOwnerNotification.replace("%old_owner%", player.getName()));
        }

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 2) {
            // Autocomplete 'confirm'
            if ("confirm".startsWith(args[1].toLowerCase())) {
                return Collections.singletonList("confirm");
            }
        }
        return Collections.emptyList();
    }
}
