package fr.euphyllia.skylliachat;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListeners implements Listener {

    private final Main plugin;

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
                    LanguageToml.sendMessage(player, LanguageToml.messagePlayerHasNotIsland);
                    return;
                }

                String message = event.getMessage();
                String format = this.plugin.getConfig().getString("chat.format", "<red>[Messaging Island] %player_name%: <gray>%message%")
                        .replace("%player_name%", player.getName())
                        .replace("%message%", message);
                for (Players islandMember : island.getMembersCached()) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(islandMember.getMojangId());
                    if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
                        LanguageToml.sendMessage(offlinePlayer.getPlayer(), format);
                    }
                }
            });
        }
    }

}
