package fr.euphyllia.skyllia.commands.admin.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
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

public class SetMaxMembersSubCommands implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(SetMaxMembersSubCommands.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!PermissionImp.hasPermission(sender, "skyllia.admins.commands.island.setmaxmembers")) {
            ConfigLoader.language.sendMessage(sender, "island.player.permission-denied");
            return true;
        }

        if (args.length < 3) {
            ConfigLoader.language.sendMessage(sender, "island.admin.max-members.args-missing");
            return true;
        }
        String playerName = args[0];
        String changeValue = args[1];
        String confirm = args[2];
        if (!confirm.equalsIgnoreCase("confirm")) {
            ConfigLoader.language.sendMessage(sender, "island.admin.max-members.no-confirm");
            return true;
        }
        try {
            UUID playerId;
            try {
                playerId = UUID.fromString(playerName);
            } catch (IllegalArgumentException ignored) {
                playerId = Bukkit.getPlayerUniqueId(playerName);
            }
            SkyblockManager skyblockManager = Skyllia.getPlugin(Skyllia.class).getInterneAPI().getSkyblockManager();
            Island island = skyblockManager.getIslandByPlayerId(playerId);
            if (island == null) {
                ConfigLoader.language.sendMessage(sender, "island.player.no-island");
                return true;
            }

            int members = Integer.parseInt(changeValue);
            boolean updated = island.setMaxMembers(members);
            if (updated) {
                ConfigLoader.language.sendMessage(sender, "island.admin.size-success");
            } else {
                ConfigLoader.language.sendMessage(sender, "island.admin.size-failed");
            }

        } catch (Exception e) {
            if (e instanceof NumberFormatException ignored) {
                ConfigLoader.language.sendMessage(sender, "island.admin.max-members-nan");
            } else {
                logger.log(Level.FATAL, e.getMessage(), e);
                ConfigLoader.language.sendMessage(sender, "island.generic.unexpected-error");
            }
        }
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!PermissionImp.hasPermission(sender, "skyllia.admins.commands.island.setmaxmembers")) {
            return Collections.emptyList();
        }

        // ARG #1 → Nom du joueur
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();
            return new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .sorted()
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            String partial = args[1].trim().toLowerCase();
            List<String> possibleValues = Arrays.asList("5", "10", "15", "20", "25");
            return possibleValues.stream()
                    .filter(value -> value.startsWith(partial))
                    .collect(Collectors.toList());
        }

        // ARG #3 → "confirm"
        else if (args.length == 3) {
            String partial = args[2].trim().toLowerCase();

            return Stream.of("confirm")
                    .filter(word -> word.startsWith(partial))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
