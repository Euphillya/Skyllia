package fr.euphyllia.skyllia.commands.admin.subcommands;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;

public class CurrentSubCommands implements SubCommandInterface {

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NonNull @NotNull String[] args) {
        if (!sender.hasPermission(permission())) {
            ConfigLoader.language.sendMessage(sender, "island.admin.current.no-permission");
            return true;
        }

        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }

        Location location = player.getLocation();

        Island island = SkylliaAPI.getIslandByChunk(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        if (island == null) {
            ConfigLoader.language.sendMessage(player, "island.admin.current.no-island-here");
            return true;
        }

        Players owner = island.getOwner();

        if (owner == null) { // Normalement impossible
            ConfigLoader.language.sendMessage(player, "island.admin.current.no-owner", Map.of(
                    "%island_id%", String.valueOf(island.getId())
            ));
            return true;
        }

        ConfigLoader.language.sendMessage(player, "island.admin.current.info", Map.of(
                "%island_id%", String.valueOf(island.getId()),
                "%owner_name%", owner.getLastKnowName(),
                "%owner_uuid%", String.valueOf(owner.getMojangId())
        ));
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NonNull @NotNull String[] args) {
        return List.of();
    }

    @Override
    public String permission() {
        return "skyllia.admins.commands.island.current";
    }
}
