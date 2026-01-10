package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;

public class BanListCommand implements SubCommandInterface {

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NonNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }

        if (!player.hasPermission(permission())) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "island.player.no-island");
            return true;
        }

        List<Players> banned = island.getBannedMembers();
        if (banned.isEmpty()) {
            ConfigLoader.language.sendMessage(player, "island.banlist.empty");
            return true;
        }

        Component message = ConfigLoader.language.translate(player, "island.banlist.title", Map.of(
                "%count%", String.valueOf(banned.size())
        ));

        for (Players ban : banned) {
            message = message.append(Component.newline()).append(
                    ConfigLoader.language.translate(player, "island.banlist.line", Map.of(
                            "%name%", ban.getLastKnowName()
                    ))
            );
        }

        player.sendMessage(message);
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NonNull String[] args) {
        return List.of();
    }

    @Override
    public String permission() {
        return "skyllia.island.command.banlist";
    }
}
