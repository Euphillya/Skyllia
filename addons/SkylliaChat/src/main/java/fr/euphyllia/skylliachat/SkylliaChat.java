package fr.euphyllia.skylliachat;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skylliachat.commands.IslandChatCommand;
import fr.euphyllia.skylliachat.configuration.ChatConfigLoader;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SkylliaChat extends JavaPlugin {

    private final ConcurrentHashMap<UUID, Boolean> islandChatEnabled = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        try {
            ChatConfigLoader.init(getDataFolder());
        } catch (Exception e) {
            getLogger().severe("Error while loading SkylliaChat config");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        SkylliaAPI.registerCommands(new IslandChatCommand(this), "chat");

        getServer().getPluginManager().registerEvents(new ChatListeners(this), this);
    }

    @Override
    public void onDisable() {
        ChatConfigLoader.unregister();
        Bukkit.getAsyncScheduler().cancelTasks(this);
        Bukkit.getGlobalRegionScheduler().cancelTasks(this);
    }

    public ConcurrentHashMap<UUID, Boolean> getIslandChatEnabled() {
        return islandChatEnabled;
    }
}
