package fr.euphyllia.skylliainfo;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skylliainfo.hook.SkylliaBankHook;
import fr.euphyllia.skylliainfo.hook.SkylliaOreHook;
import fr.euphyllia.skylliainfo.hook.SkylliaValueHook;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class IslandInfoCommand implements SubCommandInterface {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public IslandInfoCommand(Main main) {
    }


    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] arg) {
        if (!(sender instanceof Player player)) {
            LanguageToml.sendMessage(sender, LanguageToml.messageCommandPlayerOnly);
            return true;
        }

        UUID playerId = null;

        if (sender.hasPermission("skyllia.extensions.commands.is_info") && arg.length == 1) {
            playerId = Bukkit.getPlayerUniqueId(arg[0]);
        }
        if (playerId == null) {
            playerId = player.getUniqueId();
        }

        CompletableFuture<Island> islandFuture = SkylliaAPI.getIslandByPlayerId(playerId);

        islandFuture.thenAcceptAsync(island -> {
            if (island == null) {
                player.sendMessage(miniMessage.deserialize("<red>Player has not Island.</red>"));
                return;
            }
            UUID islandId = island.getId();
            String islandName = islandId.toString();
            Optional<Players> found = island.getMembers().stream().filter(players -> players.getRoleType().equals(RoleType.OWNER)).findFirst();
            Players leader = found.orElseThrow();
            double size = island.getSize();
            int maxMembers = island.getMaxMembers();
            Timestamp creationDate = island.getCreateDate();
            CopyOnWriteArrayList<Players> members = island.getMembers();
            Location location = RegionHelper.getCenterRegion(null, island.getPosition().x(), island.getPosition().z());

            player.sendMessage(miniMessage.deserialize("<gold>=== Island Information ===</gold>"));
            player.sendMessage(miniMessage.deserialize(
                    "<yellow>Island ID: </yellow><white>" + islandName + "</white>"));
            if (Bukkit.getPluginManager().isPluginEnabled("SkylliaBank")) {
                SkylliaBankHook.sendMessage(miniMessage, player, islandId);
            }
            if (Bukkit.getPluginManager().isPluginEnabled("SkylliaValue")) {
                // This plugin is not made public, because it belongs to www.excalia.fr
                // If you want to have the plugin, you will have to see with
                // their Excalia administrator (https://discord.gg/excalia)
                SkylliaValueHook.sendMessage(miniMessage, player, islandId);
            }
            player.sendMessage(miniMessage.deserialize(
                    "<yellow>Owner: </yellow><white>" + Bukkit.getOfflinePlayer(leader.getMojangId()).getName() + "</white>"));
            player.sendMessage(miniMessage.deserialize(
                    "<yellow>Size: </yellow><white>" + size + " blocks</white>"));
            player.sendMessage(miniMessage.deserialize(
                    "<yellow>Max Members: </yellow><white>" + maxMembers + "</white>"));
            player.sendMessage(miniMessage.deserialize(
                    "<yellow>Creation Date: </yellow><white>" + creationDate.toString() + "</white>"));
            player.sendMessage(miniMessage.deserialize(
                    "<yellow>Online Members: </yellow><white>"
                            + members.stream().filter(players -> Bukkit.getOfflinePlayer(players.getMojangId()).isOnline()).count()
                            + "/" + members.size() + "</white>"));
            if (Bukkit.getPluginManager().isPluginEnabled("SkylliaOre")) {
                SkylliaOreHook.sendMessage(miniMessage, player, islandId);
            }

            player.sendMessage(miniMessage.deserialize(
                    "<yellow>Location: </yellow><white>X: " + location.getBlockX() + " Z: " + location.getBlockZ() + "</white>"));

            if (!members.isEmpty()) {
                player.sendMessage(miniMessage.deserialize("<gold>=== Members ===</gold>"));
                for (Players member : members) {
                    boolean playerIsOnline = Bukkit.getOfflinePlayer(member.getMojangId()).isOnline();
                    player.sendMessage(miniMessage.deserialize(
                            (playerIsOnline ? "<green>" : "<red>") + "- [" + member.getRoleType().name() + "] " + member.getLastKnowName() + "</" + (playerIsOnline ? "green>" : "red>")));
                }
            }

        });


        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender commandSender, @NotNull String[] strings) {
        return Collections.emptyList();
    }
}
