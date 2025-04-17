package fr.euphyllia.skylliachat.commands;

import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skylliachat.SkylliaChat;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class IslandChatReloadCommand implements SubCommandInterface {

    private final SkylliaChat plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public IslandChatReloadCommand(SkylliaChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!PermissionImp.hasPermission(sender, "skylliachat.reload")) {
            sender.sendMessage(miniMessage.deserialize("<red>You are not a permission to execute this commands."));
            return true;
        }

        // Recharge la configuration
        this.plugin.reloadConfig();

        String msg = this.plugin.getConfig().getString("message.config.reloaded", "<green>Configuration successfully reloaded!");
        sender.sendMessage(miniMessage.deserialize(msg));
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}