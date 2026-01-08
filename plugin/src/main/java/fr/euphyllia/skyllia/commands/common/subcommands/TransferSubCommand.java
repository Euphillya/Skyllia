package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.event.SkyblockChangeOwnerEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TransferSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(TransferSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }

        if (args.length == 0) {
            ConfigLoader.language.sendMessage(player, "island.transfer.specify-player");
            return true;
        }

        if (!player.hasPermission("skyllia.island.command.transfer")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        String newOwnerName = args[0];

        boolean isConfirm = args.length >= 2 && args[1].equalsIgnoreCase("confirm");

        if (!isConfirm) {
            ConfigLoader.language.sendMessage(player, "island.transfer.confirm", Map.of("%new_owner%", newOwnerName));
            return true;
        }

        SkyblockManager skyblockManager = Skyllia.getInstance().getInterneAPI().getSkyblockManager();
        Island island = skyblockManager.getIslandByOwner(player.getUniqueId());

        if (island == null) {
            ConfigLoader.language.sendMessage(player, "island.player.no-island");
            return true;
        }

        Players ownerIsland = skyblockManager.getOwnerByIslandID(island);

        if (ownerIsland == null || !ownerIsland.getMojangId().equals(player.getUniqueId())) {
            ConfigLoader.language.sendMessage(player, "island.only-owner");
            return true;
        }

        Players newOwner = island.getMember(newOwnerName);

        if (newOwner == null) {
            ConfigLoader.language.sendMessage(player, "island.player.not-on-island");
            return true;
        }

        if (newOwner.getMojangId().equals(player.getUniqueId())) {
            ConfigLoader.language.sendMessage(player, "island.transfer.already-owner");
            return true;
        }

        if (!newOwner.getRoleType().equals(RoleType.CO_OWNER)) {
            ConfigLoader.language.sendMessage(player, "island.transfer.only-co-owner");
            return true;
        }

        Players oldOwner = island.getMember(player.getUniqueId());
        oldOwner.setRoleType(RoleType.CO_OWNER);
        island.updateMember(oldOwner);

        newOwner.setRoleType(RoleType.OWNER);
        island.updateMember(newOwner);

        SkyblockChangeOwnerEvent event = new SkyblockChangeOwnerEvent(island, oldOwner.getMojangId(), newOwner.getMojangId());
        Bukkit.getPluginManager().callEvent(event);

        ConfigLoader.language.sendMessage(player, "island.transfer.success", Map.of("%new_owner%", newOwner.getLastKnowName()));

        Player newOwnerPlayer = plugin.getServer().getPlayer(newOwner.getMojangId());
        if (newOwnerPlayer != null && newOwnerPlayer.isOnline()) {
            ConfigLoader.language.sendMessage(newOwnerPlayer, "island.transfer.notify-old-owner", Map.of("%old_owner%", player.getName()));
        }

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();
            if ("confirm".startsWith(partial)) {
                return Collections.singletonList("confirm");
            }
        }
        return Collections.emptyList();
    }
}
