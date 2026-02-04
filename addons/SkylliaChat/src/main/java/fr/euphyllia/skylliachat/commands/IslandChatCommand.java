package fr.euphyllia.skylliachat.commands;

import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliachat.SkylliaChat;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class IslandChatCommand implements SubCommandInterface {

    private final SkylliaChat plugin;

    public IslandChatCommand(SkylliaChat main) {
        plugin = main;
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }
        if (!sender.hasPermission("skylliachat.use")) {
            ConfigLoader.language.sendMessage(player, "addons.chat.no-permission");
            return true;
        }
        final UUID uuid = player.getUniqueId();

        // Toggle island chat mode
        boolean isEnabled = this.plugin.getIslandChatEnabled().getOrDefault(uuid, false);
        this.plugin.getIslandChatEnabled().put(uuid, !isEnabled);

        if (isEnabled) {
            ConfigLoader.language.sendMessage(player, "addons.chat.disabled");
        } else {
            ConfigLoader.language.sendMessage(player, "addons.chat.enabled");
        }
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
