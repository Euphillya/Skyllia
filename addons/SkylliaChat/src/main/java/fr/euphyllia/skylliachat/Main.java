package fr.euphyllia.skylliachat;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skylliachat.commands.IslandChatCommand;
import fr.euphyllia.skylliachat.commands.IslandChatReloadCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ConcurrentHashMap;

public final class Main extends JavaPlugin {

    private final ConcurrentHashMap<Player, Boolean> islandChatEnabled = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        // Plugin startup logic
        SkylliaAPI.registerCommands(new IslandChatCommand(this), "chat");
        SkylliaAPI.registerAdminCommands(new IslandChatReloadCommand(this), "chat_reload");

        getServer().getPluginManager().registerEvents(new ChatListeners(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public ConcurrentHashMap<Player, Boolean> getIslandChatEnabled() {
        return islandChatEnabled;
    }
}
