package fr.euphyllia.skylliachat.commands;

import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skylliachat.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class IslandChatReloadCommand implements SubCommandInterface {

    private final Main plugin;

    public IslandChatReloadCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("skylliachat.reload")) {
            LanguageToml.sendMessage(sender, "<red>You are not a permission to execute this commands.");
            return true;
        }

        // Recharge la configuration
        this.plugin.reloadConfig();

        String msg = this.plugin.getConfig().getString("message.config.reloaded", "<green>Configuration successfully reloaded!");
        LanguageToml.sendMessage(sender, msg);
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}