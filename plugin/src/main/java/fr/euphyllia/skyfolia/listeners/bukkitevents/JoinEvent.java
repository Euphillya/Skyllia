package fr.euphyllia.skyfolia.listeners.bukkitevents;

import fr.euphyllia.skyfolia.api.InterneAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class JoinEvent implements Listener {

    private final InterneAPI api;

    public JoinEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler
    public void onLoadIslandInJoinEvent(PlayerJoinEvent playerJoinEvent) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try {
            executor.execute(() -> {

            });
        } finally {
            executor.shutdown();
        }
    }

}
