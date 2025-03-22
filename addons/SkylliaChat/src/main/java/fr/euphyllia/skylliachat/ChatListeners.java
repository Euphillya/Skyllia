package fr.euphyllia.skylliachat;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListeners implements Listener {

    private final Main plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ChatListeners(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (Main.getPlugin(Main.class).getIslandChatEnabled().getOrDefault(player, false)) {
            event.setCancelled(true);

            Bukkit.getAsyncScheduler().runNow(Main.getPlugin(Main.class), scheduledTask -> {
                Island island = SkylliaAPI.getCacheIslandByPlayerId(player.getUniqueId());
                if (island == null) {
                    ConfigLoader.language.sendMessage(player, "island.player.no-island");
                    return;
                }

                String message = event.getMessage();
                String format = this.plugin.getConfig().getString("chat.format", "<red>[Messaging Island] %player_name%: <gray>%message%")
                        .replace("%player_name%", player.getName())
                        .replace("%message%", message);

                // MiniMessage doesn't like legacy formatting codes...
                format = ChatColor.translateAlternateColorCodes('&', format)
                        .replace("§0", "<black>")
                        .replace("§1", "<dark_blue>")
                        .replace("§2", "<dark_green>")
                        .replace("§3", "<dark_aqua>")
                        .replace("§4", "<dark_red>")
                        .replace("§5", "<dark_purple>")
                        .replace("§6", "<gold>")
                        .replace("§7", "<gray>")
                        .replace("§8", "<dark_gray>")
                        .replace("§9", "<blue>")
                        .replace("§a", "<green>")
                        .replace("§b", "<aqua>")
                        .replace("§c", "<red>")
                        .replace("§d", "<light_purple>")
                        .replace("§e", "<yellow>")
                        .replace("§f", "<white>")
                        .replace("§r", "<reset>")
                        .replace("§k", "<obfuscated>")
                        .replace("§l", "<bold>")
                        .replace("§m", "<strikethrough>")
                        .replace("§n", "<underlined>")
                        .replace("§o", "<italic>");


                for (Players islandMember : island.getMembersCached()) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(islandMember.getMojangId());
                    if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
                        offlinePlayer.getPlayer().sendMessage(miniMessage.deserialize(format));
                    }
                }
            });
        }
    }

}
