package fr.euphyllia.skyllia.commands.admin.subcommands;

import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.event.IslandInfoEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class InfoSubCommand implements SubCommandInterface {
    /**
     * Handles the execution of a sub-command.
     *
     * @param plugin the {@link Plugin} instance that is executing the command
     * @param sender the {@link CommandSender} who issued the command
     * @param args   the arguments provided with the command
     * @return {@code true} if the command was successfully handled, {@code false} otherwise
     */
    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }

        if (!PermissionImp.hasPermission(player, permission())) {
            ConfigLoader.language.sendMessage(player, "island.info.no-permission");
            return true;
        }
        UUID playerId;
        if (args.length == 1) {
            playerId = Bukkit.getPlayerUniqueId(args[0]);
        } else {
            playerId = player.getUniqueId();
        }

        CompletableFuture<Island> futureIsland = SkylliaAPI.getIslandByPlayerId(playerId);

        futureIsland.thenAcceptAsync(island -> {
            if (island == null) {
                ConfigLoader.language.sendMessage(player, "island.player.no-island");
                return;
            }

            UUID islandId = island.getId();
            Optional<Players> found = island.getMembers().stream().filter(p -> p.getRoleType() == RoleType.OWNER).findFirst();
            Players leader = found.orElse(null);
            if (leader == null) {
                ConfigLoader.language.sendMessage(player, "island.info.no-owner");
                return;
            }

            Location center = RegionHelper.getCenterRegion(null, island.getPosition().x(), island.getPosition().z());
            Timestamp createdAt = island.getCreateDate();
            double size = island.getSize();
            int maxMembers = island.getMaxMembers();
            CopyOnWriteArrayList<Players> members = island.getMembers();

            Component infoComponent = Component.text("")
                    .append(ConfigLoader.language.translate(player, "island.info.display.title")).append(Component.newline())
                    .append(ConfigLoader.language.translate(player, "island.info.display.id", Map.of("%id%", islandId.toString()))).append(Component.newline())
                    .append(ConfigLoader.language.translate(player, "island.info.display.owner", Map.of("%owner%", leader.getLastKnowName()))).append(Component.newline())
                    .append(ConfigLoader.language.translate(player, "island.info.display.size", Map.of("%size%", String.valueOf(size)))).append(Component.newline())
                    .append(ConfigLoader.language.translate(player, "island.info.display.max-members", Map.of("%max%", String.valueOf(maxMembers)))).append(Component.newline())
                    .append(ConfigLoader.language.translate(player, "island.info.display.created", Map.of("%created%", createdAt.toString()))).append(Component.newline())
                    .append(ConfigLoader.language.translate(player, "island.info.display.online-members", Map.of(
                            "%online%", String.valueOf(members.stream().filter(p -> Bukkit.getOfflinePlayer(p.getMojangId()).isOnline()).count()),
                            "%total%", String.valueOf(members.size())
                    ))).append(Component.newline())
                    .append(ConfigLoader.language.translate(player, "island.info.display.location", Map.of(
                            "%x%", String.valueOf(center.getBlockX()),
                            "%z%", String.valueOf(center.getBlockZ())
                    )));


            IslandInfoEvent infoEvent = new IslandInfoEvent(player, island);
            infoEvent.callEvent();
            for (Component extra : infoEvent.getExtraMessages()) {
                infoComponent = infoComponent.append(Component.newline()).append(extra);
            }

            if (!members.isEmpty()) {
                infoComponent = infoComponent.append(Component.newline()).append(ConfigLoader.language.translate(player, "island.info.display.members-title"));
                for (Players member : members) {
                    boolean online = Bukkit.getOfflinePlayer(member.getMojangId()).isOnline();
                    String color = online ? "<green>" : "<red>";
                    infoComponent = infoComponent.append(Component.newline()).append(
                            ConfigLoader.language.translate(player, "island.info.display.member-line", Map.of(
                                    "%color%", color,
                                    "%role%", member.getRoleType().name(),
                                    "%name%", member.getLastKnowName()
                            ))
                    );
                }
            }

            player.sendMessage(infoComponent);
        });

        return true;
    }

    /**
     * Provides tab completion suggestions for the sub-command.
     *
     * @param plugin the {@link Plugin} instance that is executing the command
     * @param sender the {@link CommandSender} who is tab completing
     * @param args   the arguments provided with the command so far
     * @return a {@link List} of suggestions for tab completion, or {@code null} if no suggestions are available
     */
    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }

    @Override
    public String permission() {
        return "skyllia.admins.commands.island.info";
    }
}
