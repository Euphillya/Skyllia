package fr.euphyllia.skylliachat.commands;

import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skylliachat.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class IslandChatCommand implements SubCommandInterface {

    private final Main plugin;

    public IslandChatCommand(Main main) {
        plugin = main;
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            LanguageToml.sendMessage(sender, LanguageToml.messageCommandPlayerOnly);
            return true;
        }
        if (!sender.hasPermission("skylliachat.use")) {
            LanguageToml.sendMessage(sender, "<red>You are not a permission to execute this commands.");
            return true;
        }

        // Toggle island chat mode
        boolean isEnabled = this.plugin.getIslandChatEnabled().getOrDefault(player, false);
        this.plugin.getIslandChatEnabled().put(player, !isEnabled);

        String msg;
        if (isEnabled) {
            msg = this.plugin.getConfig().getString("message.chat.disabled", "<red>Island messaging Disabled.");
        } else {
            msg = this.plugin.getConfig().getString("message.chat.enabled", "<green>Island Messaging Enabled.");
        }
        LanguageToml.sendMessage(sender, msg);
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
