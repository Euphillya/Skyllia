package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.api.PermissionImp;
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
import java.util.concurrent.CompletableFuture;

public class BanListCommand implements SubCommandInterface {

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NonNull @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }

        if (!PermissionImp.hasPermission(player, permission())) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        CompletableFuture<Island> futureIsland = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());

        futureIsland.thenAcceptAsync(island -> {
            if (island == null) {
                ConfigLoader.language.sendMessage(player, "island.player.no-island");
                return;
            }

            List<Players> banned = island.getBannedMembers();

            if (banned.isEmpty()) {
                ConfigLoader.language.sendMessage(player, "island.banlist.empty");
                return;
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
        });

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NonNull @NotNull String[] args) {
        return List.of();
    }

    @Override
    public String permission() {
        return "skyllia.island.command.banlist";
    }
}
