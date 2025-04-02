package fr.euphyllia.skylliachat.commands;

import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliachat.Main;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class IslandChatCommand implements SubCommandInterface {

    private final Main plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public IslandChatCommand(Main main) {
        plugin = main;
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }
        if (!PermissionImp.hasPermission(sender, "skylliachat.use")) {
            sender.sendMessage(miniMessage.deserialize("<red>You are not a permission to execute this commands."));
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
        sender.sendMessage(miniMessage.deserialize(msg));
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
