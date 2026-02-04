package fr.euphyllia.skylliachat;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skylliachat.commands.IslandChatCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SkylliaChat extends JavaPlugin {

    private final ConcurrentHashMap<UUID, Boolean> islandChatEnabled = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        SkylliaAPI.registerCommands(new IslandChatCommand(this), "chat");

        getServer().getPluginManager().registerEvents(new ChatListeners(this), this);
    }

    @Override
    public void onDisable() {
        Bukkit.getAsyncScheduler().cancelTasks(this);
        Bukkit.getGlobalRegionScheduler().cancelTasks(this);
    }

    public ConcurrentHashMap<UUID, Boolean> getIslandChatEnabled() {
        return islandChatEnabled;
    }
}
